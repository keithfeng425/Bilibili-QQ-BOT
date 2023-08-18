package top.keithfeng.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.ID;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.OriginBotManager;
import love.forte.simbot.definition.Group;
import love.forte.simbot.message.AtAll;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import love.forte.simbot.resources.FileResource;
import love.forte.simbot.resources.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;

@Slf4j
@Component
public class LiveUtil {

    @Value("${bilibili.group}")
    private Long groupId;

    @Value("${bilibili.room}")
    private Long roomId;

    @Value("${notify-all.live-on}")
    private boolean notifyAllOn;

    @Value("${notify-all.live-off}")
    private boolean notifyAllOff;

    @Autowired
    private OriginBotManager botManager;

    private static boolean liveFlag = false;

    private static String CLASS_PATH = null;

    static {
        try {
            CLASS_PATH = ResourceUtils.getFile("").getAbsolutePath() + "/";
        } catch (FileNotFoundException e) {
            log.error("获取工作目录失败！");
        }
    }

    public JSONObject getRoomInfo(String roomLink) {
        try {
            String body = HttpUtil.createGet(roomLink)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .execute().body();

            String jsonStr = body.split("NEPTUNE_IS_MY_WAIFU__=")[2].split("</script><script>")[0];
            return JSONUtil.parseObj(jsonStr);
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warn("页面读取有误，3秒后重试……");
            return null;
        }
    }

    @SneakyThrows
    @Scheduled(fixedDelay = 3000)
    public void liveRoomListener() {
        try {
            Bot bot = botManager.getAnyBot();
            Group group = bot.getGroup(ID.$(groupId));

            String liveStatusApi = "https://api.live.bilibili.com/xlive/web-room/v2/index/getRoomPlayInfo?room_id=" + roomId + "&protocol=0,1&format=0,1,2&codec=0,1&qn=0&platform=web&ptype=8&dolby=5&panorama=1";
            String response = HttpUtil.createGet(liveStatusApi)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .execute().body();
            JSONObject mainBody = JSONUtil.parseObj(response);
            int liveStatus = mainBody.getJSONObject("data").getInt("live_status");
            String roomLink = "https://live.bilibili.com/" + roomId;

            switch (liveStatus) {
                case 1:
                    if (liveFlag) {
                        // 直播状态为真时，什么都不做
                    } else {
                        // 修改直播状态为真，输出通知
                        liveFlag = true;

                        JSONObject mainContent = getRoomInfo(roomLink);
                        JSONObject anchorInfo = mainContent.getJSONObject("roomInfoRes").getJSONObject("data").getJSONObject("anchor_info");
                        String username = anchorInfo.getJSONObject("base_info").getStr("uname");
                        JSONObject roomInfo = mainContent.getJSONObject("roomInfoRes").getJSONObject("data").getJSONObject("room_info");
                        String title = roomInfo.getStr("title");
                        String imgSrc = UnicodeUtil.toString(roomInfo.getStr("cover"));
                        String areaName = new StringBuilder().append("[")
                                .append(roomInfo.getStr("parent_area_name"))
                                .append("/")
                                .append(roomInfo.getStr("area_name"))
                                .append("]")
                                .toString();

                        System.out.println(username + " 正在开播：" + areaName + " " + title);
                        System.out.println(imgSrc);
                        System.out.println(roomLink);

                        MessagesBuilder messages = new MessagesBuilder()
                                .append(username)
                                .append(" 正在开播：")
                                .append("\n")
                                .append(areaName)
                                .append(" ")
                                .append(title);
                        if (notifyAllOn) {
                            messages.append(" ").append(AtAll.INSTANCE);
                        }

                        // 下载封面
                        HttpUtil.downloadFile(imgSrc, CLASS_PATH + "cover.jpg");
                        FileResource imgResource = Resource.of(new File(CLASS_PATH + "cover.jpg"));
                        messages.append("\n")
                                .image(imgResource)
                                .append("\n")
                                .append(roomLink);

                        Messages message = messages.build();
                        group.sendBlocking(message);
                        // 删除临时图片
                        FileUtil.del(CLASS_PATH + "cover.jpg");
                    }
                    break;
                case 2:
                    if (!liveFlag) {
                        // 直播状态为假时，什么都不做
                    } else {
                        // 修改直播状态为假，输出通知
                        liveFlag = false;

                        JSONObject mainContent = getRoomInfo(roomLink);
                        JSONObject anchorInfo = mainContent.getJSONObject("roomInfoRes").getJSONObject("data").getJSONObject("anchor_info");
                        String username = anchorInfo.getJSONObject("base_info").getStr("uname");
                        System.out.println(username + " 下播了");
                        MessagesBuilder messages = new MessagesBuilder()
                                .append(username).append(" 下播了");
                        if (notifyAllOff) {
                            messages.append(" ").append(AtAll.INSTANCE);
                        }
                        Messages message = messages.build();
                        group.sendBlocking(message);
                    }
                    break;
            }
        } catch (NullPointerException ignore) {

        } catch (Exception e) {
            liveFlag = false;
            throw e;
        }
    }
}
