package top.keithfeng.listener;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simbot.definition.Friend;
import love.forte.simbot.event.EventResult;
import love.forte.simbot.event.FriendMessageEvent;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.message.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 *
 * 一个自定义的监听函数载体类，直接通过 {@link Component} 注册到Spring中即可。
 * 在此类中通过标记 {@link love.forte.simboot.annotation.Listener} 来开始编写监听函数。
 *
 * @author ForteScarlet
 */
@Slf4j
@Component
public class MyListener {

    @Value("${gpt.api-key}")
    private String gptApiKey;

    @Value("${gpt.use-api2d}")
    private boolean useApi2d;

    public String sendMessageToChatGpt(String textContent) {
        if (StrUtil.isEmpty(gptApiKey)) {
            log.warn("未填写ChatGPT APIKey，将调用青云客API进行回复……");
            String qykApi = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + textContent;
            HttpResponse response = HttpUtil.createGet(qykApi).execute();
            JSONObject responseBody = JSONUtil.parseObj(response.body());
            return responseBody.getStr("content");
        }
        String apiUrl;
        if (useApi2d) {
            apiUrl = "https://openai.api2d.net/v1/chat/completions";
            log.info("[ChatGpt] 使用API2D代理服务中……");
        } else {
            apiUrl = "https://api.openai.com/v1/chat/completions";
            log.info("[ChatGpt] 使用OpenAI官方服务中……");
        }

        Map<String, Object> requestBody = new HashMap<>(2);
        requestBody.put("model", "gpt-3.5-turbo");
        List<Map<String,String>> messageList = new ArrayList<>();
        Map<String, String> messageMap = new HashMap<>(2);
        messageMap.put("role", "user");
        messageMap.put("content", textContent);
        messageList.add(messageMap);
        requestBody.put("messages", messageList);

        Element body;
        try {
            body = Jsoup.connect(apiUrl)
                    .ignoreContentType(true)
                    .timeout(100000)
                    .header("Authorization", "Bearer " + gptApiKey)
                    .header("Content-Type", "application/json")
                    .requestBody(JSONUtil.toJsonStr(requestBody))
                    .post().body();
        } catch (Exception e) {
            log.error("[ChatGPT] 服务异常，请检查网络……", e);
            return "[ChatGPT] 我的信号不太好，暂时没办法回复你哦……";
        }

        JSONObject responseBody = JSONUtil.parseObj(body.text());
        JSONObject choice = responseBody.getJSONArray("choices").get(0, JSONObject.class);
        String answer = choice.getJSONObject("message").getStr("content");
        System.out.println("[ChatGPT] " + answer);
        return "[ChatGPT] " + answer;
    }


    @Listener
    @ContentTrim
    public void groupMessageListener(GroupMessageEvent event) {
        Messages messages = event.getMessageContent().getMessages();
        boolean isTarget = false;
        for (Message.Element<?> message : messages) {
            if (message instanceof At && event.getBot().getId().equals(((At) message).getTarget())) {
                isTarget = true;
            }
        }
        if (isTarget) {
            event.replyAsync("[ChatGPT] 我正在组织语言中，可能耗时较久，请耐心等待一下哦～");
            StringBuffer buffer = new StringBuffer();
            for (Message.Element<?> message : messages) {
                if (message instanceof Text) {
                    String text = ((Text) message).getText();
                    buffer.append(text.trim()).append(" ");
                }
            }
            String textContent = buffer.toString();
            event.replyAsync(sendMessageToChatGpt(textContent));

        }
    }

    /**
     * 这个监听函数我们实现：
     * <p>好友发送一句: 你好
     * <p>就回复一句: 你也好
     * <p>
     * 此监听函数将会使用 <b>阻塞的</b> 代码风格。
     */
    @Listener
    @Filter("你好") // 简单的事件过滤可以直接通过 @Filter 来完成。其更多参数请参考此注解的各注释说明
    @ContentTrim   // @ContentTrim 会在 @Filter 进行文本匹配的时候，消除其可能存在的前后空字符（默认是不会的），
                   // 这对于可能掺杂其他类型的消息元素时候很有用（例如At，或者表情）
    public void helloListener(FriendMessageEvent event) {
        // 因为要监听'好友消息'，因此此处参数为 FriendMessageEvent
        // 当此方法被触发的时候，就说明好友发送的消息就是 '你好' 了, 此处直接回复 '你也好'

        // 此处有多种方式。
        // 方式1: 获取此好友并直接发送
        Friend friend = event.getFriend();
        friend.sendBlocking("你也好");

        // 方式2: 使用消息事件对象提供的'回复'(reply) 能力
        event.replyBlocking("你也好");
    }

    /**
     * 这个监听函数我们实现：
     * <p> 当好友发送了一个带有图片的消息时，我们回复一句'哇！是图片'，然后再将这个图片发送回去，并且重复发送3次。
     * <p>
     * 此监听函数将会使用 <b>异步的</b> 代码风格，也是推荐使用的方式。异步的代码可以拥有更高的资源利用率，也不存在发生线程阻塞的风险（正确使用的情况下）
     */
    @Listener
    public CompletableFuture<?> imgMessageListener(FriendMessageEvent event) {
        // 因为要监听'好友消息'，因此此处参数为 FriendMessageEvent
        // 判断消息类型是比较复杂的判断逻辑，因此暂时不能使用@Filter一笔带过。

        // 从消息正文（MessageContent）中得到消息链
        ReceivedMessageContent messageContent = event.getMessageContent();
        Messages messages = messageContent.getMessages();
        // 寻找消息链中是否存在 Image 类型消息
        // 你可以使用 get(Message.Key), 也可以自行循环寻找. 前者的结果为列表，而使用后者则可以稍稍的节省一点点的资源
        List<Image<?>> images = messages.get(Image.Key);
        if (images.isEmpty()) {
            // 如果为空，则代表没找到图片，放弃, 返回一个 CompletableFuture<EventResult.Invalid>
            // EventResult.Invalid 即代表‘本次监听结果无效’
            return CompletableFuture.completedFuture(EventResult.invalid());
        }

        // 存在图片，此处暂且假设只有一个图片。得到它
        Image<?> image = images.get(0);

        // 得到好友对象，
        return event.getFriendAsync()
                // 向其回复消息 '哇！是图片'
                // 并在消息发送成功后，像后续继续传递 friend 对象
                .thenCompose(friend -> friend.sendAsync("哇！是图片").thenApply(receipt -> friend))
                .thenCompose(friend -> {
                    // 将那个图片重复三次并构建为消息发送
                    MessagesBuilder builder = new MessagesBuilder();
                    for (int i = 0; i < 3; i++) {
                        builder.append(image);
                    }
                    Messages imagesMessage = builder.build();
                    return friend.sendAsync(imagesMessage);
                })
                // 因为目前用不到，所以抛弃 MessageReceipt 的结果，使得最后结果为 Void。
                .thenAccept(receipt -> {});

        // 此监听函数将会在上述的三张图片发送成功后才会继续后续的其他监听函数，
        // 因为当前监听函数返回的类型是 CompletableFuture.
        // 当一个监听的最终结果为 CompletableFuture 时，其执行时会挂起等待最终结果，而后才会进行后续监听函数的执行。
        // 但是不同于Blocking相关的API，挂起并不会阻塞其他线程，这与Kotlin协程有关。

        // 因此上述代码实质上依旧会安全的按照“顺序”执行，并在消息全部发送成功（完成）后继续，不会造成监听函数的顺序混乱。
        // 当然，如果你不关心这种异步顺序，也可以不管它。


    }

}
