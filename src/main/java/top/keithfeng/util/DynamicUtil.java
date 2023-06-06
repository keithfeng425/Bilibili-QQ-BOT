package top.keithfeng.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.ID;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.OriginBotManager;
import love.forte.simbot.definition.Group;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import love.forte.simbot.resources.FileResource;
import love.forte.simbot.resources.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import top.keithfeng.domain.DynamicHistory;
import top.keithfeng.mapper.DynamicHistoryMapper;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;

@Slf4j
@Component
// public class DynamicUtil implements ApplicationListener<ApplicationStartedEvent> {
public class DynamicUtil {

    @Autowired
    private DynamicHistoryMapper dynamicHistoryMapper;

    @Value("${bilibili.uid}")
    private String uid;

    @Value("${bilibili.group}")
    private Long groupId;

    private Bot bot = null;
    private OriginBotManager botManager = null;

    private static String CLASS_PATH = null;
    static {
        try {
            CLASS_PATH = ResourceUtils.getFile("").getAbsolutePath() + "/";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        botManager = OriginBotManager.INSTANCE;
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 3000)
    public void bilibiliListener() {
        bot = botManager.getAnyBot();
        String spaceApi = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid=" + uid + "&need_top=0";
        Group group = bot.getGroup(ID.$(groupId));

        String response = HttpUtil.createGet(spaceApi)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .execute().body();
        JSONObject mainBody = JSONUtil.parseObj(response);
        JSONArray timeline = mainBody.getJSONObject("data").getJSONArray("cards");
        JSONObject card = timeline.getJSONObject(0);
        Long dynamicId = card.getJSONObject("desc").getLong("dynamic_id");

        // 如果当前动态ID已经保存进数据库，则本次不再提醒
        DynamicHistory history = dynamicHistoryMapper.selectByPrimaryKey(dynamicId);
        if (history != null) {
            return;
        }

        String dynamicLink = "https://m.bilibili.com/dynamic/" + dynamicId;
        String cardContent = card.getStr("card");
        JSONObject cardObject = JSONUtil.parseObj(cardContent);
        JSONObject item = cardObject.getJSONObject("item");
        JSONObject origin = cardObject.getJSONObject("origin");
        if (item != null && origin == null) {
            String content = item.getStr("description");
            String username = cardObject.getJSONObject("user").getStr("name");
            long time;
            try {
                time = item.getLong("timestamp") * 1000;
            } catch (NullPointerException e) {
                time = item.getLong("upload_time") * 1000;
            }
            Timestamp timestamp = new Timestamp(time);
            String datetime = DateUtil.format(timestamp, "yyyy-MM-dd HH:mm:ss");
            Object pictures = item.getJSONArray("pictures").get(0);
            String imgSrc = ((JSONObject) pictures).getStr("img_src");
            HttpUtil.downloadFile(imgSrc, CLASS_PATH + "dynamic.jpg");
            System.out.println(username + " 于 " + datetime + " 发表了新动态：\n" + content);
            System.out.println(imgSrc);
            FileResource imgResource = Resource.of(new File(CLASS_PATH + "dynamic.jpg"));

            MessagesBuilder messages = new MessagesBuilder()
                    .append(username)
                    .append(" 于 ")
                    .append(datetime)
                    .append(" 发表了新动态：\n")
                    .append(content)
                    .append("\n")
                    .image(imgResource)
                    .append("\n")
                    .append(dynamicLink);
            Messages message = messages.build();
            group.sendBlocking(message);
            // 删除临时图片
            FileUtil.del(CLASS_PATH + "dynamic.jpg");
            // 插入数据库
            dynamicHistoryMapper.insert(new DynamicHistory(dynamicId, "动态", message));

        } else if (item == null && origin == null) {
            String title = cardObject.getStr("title");
            String desc = cardObject.getStr("desc");
            String dynamic = cardObject.getStr("dynamic");
            JSONObject owner = cardObject.getJSONObject("owner");
            if (owner != null) {
                String username = owner.getStr("name");
                String pic = cardObject.getStr("pic");
                long time = cardObject.getLong("ctime") * 1000;
                Timestamp timestamp = new Timestamp(time);
                String datetime = DateUtil.format(timestamp, "yyyy-MM-dd HH:mm:ss");
                System.out.println(username + " 于 " + datetime + " 发表了新投稿 " + title);
                System.out.println(desc);
                System.out.println("UP主留言：" + dynamic);
                System.out.println(pic);
                HttpUtil.downloadFile(pic, CLASS_PATH + "cover.jpg");
                FileResource imgResource = Resource.of(new File(CLASS_PATH + "cover.jpg"));

                MessagesBuilder messages = new MessagesBuilder()
                        .append(username)
                        .append(" 于 ")
                        .append(datetime)
                        .append(" 发表了新投稿：")
                        .append("\n")
                        .append(title)
                        .append("\n")
                        .append(desc)
                        .append("\n")
                        .append("UP主留言：")
                        .append("\n")
                        .append(dynamic)
                        .append("\n")
                        .image(imgResource);
                Messages message = messages.build();
                group.sendBlocking(message);
                // 删除临时图片
                FileUtil.del(CLASS_PATH + "cover.jpg");
                // 插入数据库
                dynamicHistoryMapper.insert(new DynamicHistory(dynamicId, "投稿", message));
            } else {
                String colTitle = cardObject.getStr("title");
                String colAuthor = cardObject.getJSONObject("author").getStr("name");
                long time = cardObject.getLong("publish_time") * 1000;
                Timestamp timestamp = new Timestamp(time);
                String datetime = DateUtil.format(timestamp, "yyyy-MM-dd HH:mm:ss");
                String colSummary = cardObject.getStr("summary");
                JSONArray imageUrls = cardObject.getJSONArray("image_urls");
                String imgSrc = imageUrls.get(0).toString();

                System.out.println(colAuthor + " 于 " + datetime + " 投稿了专栏：");
                System.out.println(colTitle);
                System.out.println(colSummary);

                HttpUtil.downloadFile(imgSrc, CLASS_PATH + "column.jpg");
                FileResource imgResource = Resource.of(new File(CLASS_PATH + "column.jpg"));

                MessagesBuilder messages = new MessagesBuilder()
                        .append(colAuthor)
                        .append(" 于 ")
                        .append(datetime)
                        .append(" 投稿了专栏：")
                        .append("\n")
                        .append(colTitle)
                        .append("\n")
                        .append(colSummary)
                        .append("...")
                        .append("\n")
                        .image(imgResource)
                        .append("\n")
                        .append(dynamicLink);
                Messages message = messages.build();
                group.sendBlocking(message);
                // 删除临时图片
                FileUtil.del(CLASS_PATH + "column.jpg");
                // 插入数据库
                dynamicHistoryMapper.insert(new DynamicHistory(dynamicId, "专栏", message));
            }
        } else {
            JSONObject user = cardObject.getJSONObject("user");
            JSONObject originOwner = origin.getJSONObject("owner");

            String content = item.getStr("content");
            String username = user.getStr("uname");
            long time = card.getJSONObject("desc").getLong("timestamp") * 1000;
            Timestamp timestamp = new Timestamp(time);
            String datetime = DateUtil.format(timestamp, "yyyy-MM-dd HH:mm:ss");

            if (originOwner == null) {

                JSONObject originUser = origin.getJSONObject("user");
                if (originUser != null) {
                    String originName = originUser.getStr("name");
                    String originContent = origin.getJSONObject("item").getStr("description");

                    JSONArray pictures = origin.getJSONObject("item").getJSONArray("pictures");
                    JSONObject imgObject = pictures.getJSONObject(0);
                    String imgSrc = imgObject.getStr("img_src");

                    System.out.println(username + " 于 " + datetime + " 转发了 " + originName + " 的动态：");
                    System.out.println(originContent);
                    System.out.println("UP主留言：" + content);
                    System.out.println(imgSrc);

                    MessagesBuilder messages = new MessagesBuilder()
                            .append(username)
                            .append(" 于 ")
                            .append(datetime)
                            .append(" 转发了 ")
                            .append(originName)
                            .append(" 的动态：")
                            .append("\n")
                            .append(originContent)
                            .append("\n")
                            .append("UP主留言：")
                            .append(content)
                            .append("\n");

                    HttpUtil.downloadFile(imgSrc, CLASS_PATH + "dynamic.jpg");
                    FileResource imgResource = Resource.of(new File(CLASS_PATH + "dynamic.jpg"));
                    messages.image(imgResource)
                            .append("\n")
                            .append(dynamicLink);
                    Messages message = messages.build();
                    group.sendBlocking(message);
                    // 删除临时图片
                    FileUtil.del(CLASS_PATH + "dynamic.jpg");
                    // 插入数据库
                    dynamicHistoryMapper.insert(new DynamicHistory(dynamicId, "转发动态", message));
                } else {
                    JSONObject livePlayInfo = origin.getJSONObject("live_play_info");
                    if (livePlayInfo != null) {
                        String liverName = cardObject.getJSONObject("origin_user").getJSONObject("info").getStr("uname");

                        String areaName = livePlayInfo.getStr("area_name");
                        String title = livePlayInfo.getStr("title");
                        String roomLink = livePlayInfo.getStr("link");
                        String imgSrc = livePlayInfo.getStr("cover");
                        System.out.println(username + " 于 " + datetime + " 转发了 " + liverName + " 的直播间：[" + areaName + "] " + title);
                        System.out.println("直播间地址：" + roomLink);
                        System.out.println(imgSrc);

                        HttpUtil.downloadFile(imgSrc, CLASS_PATH + "cover.jpg");

                        FileResource imgResource = Resource.of(new File(CLASS_PATH + "cover.jpg"));
                        MessagesBuilder messages = new MessagesBuilder()
                                .append(username)
                                .append(" 于 ")
                                .append(datetime)
                                .append(" 转发了 ")
                                .append(liverName)
                                .append(" 的直播间：[")
                                .append(areaName)
                                .append("] ")
                                .append(title)
                                .append("\n")
                                .append("直播间地址：")
                                .append(roomLink)
                                .append("\n")
                                .image(imgResource);
                        Messages message = messages.build();
                        group.sendBlocking(message);
                        // 删除临时图片
                        FileUtil.del(CLASS_PATH + "cover.jpg");
                        // 插入数据库
                        dynamicHistoryMapper.insert(new DynamicHistory(dynamicId, "转发直播", message));
                    } else {
                        String originName = origin.getJSONObject("author").getStr("name");
                        String title = origin.getStr("title");
                        String originSummary = origin.getStr("summary");

                        System.out.println(username + " 于 " + datetime + " 转发了 " + originName + " 的专栏：");
                        System.out.println(title);
                        System.out.println(originSummary);
                        System.out.println("UP主留言：" + content);

                        MessagesBuilder messages = new MessagesBuilder()
                                .append(username)
                                .append(" 于 ")
                                .append(datetime)
                                .append(" 转发了 ")
                                .append(originName)
                                .append(" 的专栏：")
                                .append("\n")
                                .append(title)
                                .append("\n")
                                .append(originSummary)
                                .append("...")
                                .append("\n")
                                .append("UP主留言：")
                                .append(content)
                                .append("\n");

                        JSONArray pictures = origin.getJSONArray("image_urls");
                        String imgSrc = pictures.get(0).toString();
                        HttpUtil.downloadFile(imgSrc, CLASS_PATH + "column.jpg");
                        FileResource imgResource = Resource.of(new File(CLASS_PATH + "column.jpg"));
                        messages.image(imgResource)
                                .append("\n")
                                .append(dynamicLink);
                        Messages message = messages.build();
                        group.sendBlocking(message);
                        // 删除临时图片
                        FileUtil.del(CLASS_PATH + "column.jpg");
                        // 插入数据库
                        dynamicHistoryMapper.insert(new DynamicHistory(dynamicId, "转发专栏", message));
                    }
                }

            } else {
                String originName = originOwner.getStr("name");
                String title = origin.getStr("title");
                String desc = origin.getStr("desc");
                String videoLink = origin.getStr("short_link_v2");
                String imgSrc = origin.getStr("pic");

                HttpUtil.downloadFile(imgSrc, CLASS_PATH + "repost.jpg");
                FileResource imgResource = Resource.of(new File(CLASS_PATH + "repost.jpg"));

                System.out.println(username + " 于 " + datetime + " 转发了 " + originName + " 的投稿：" + title);
                System.out.println(desc + " " + videoLink);
                System.out.println(imgSrc);
                System.out.println("UP主留言：" + content);

                MessagesBuilder messages = new MessagesBuilder()
                        .append(username)
                        .append(" 于 ")
                        .append(datetime)
                        .append(" 转发了 ")
                        .append(originName)
                        .append(" 的投稿：")
                        .append("\n")
                        .append(title)
                        .append("\n")
                        .append(desc)
                        .append("\n")
                        .append(videoLink)
                        .append("\n")
                        .append("UP主留言：")
                        .append(content)
                        .append("\n")
                        .image(imgResource)
                        .append(dynamicLink);
                Messages message = messages.build();
                group.sendBlocking(message);
                // 删除临时图片
                FileUtil.del(CLASS_PATH + "repost.jpg");
                // 插入数据库
                dynamicHistoryMapper.insert(new DynamicHistory(dynamicId, "转发投稿", message));
            }

        }
        System.out.println(dynamicLink);
    }
}
