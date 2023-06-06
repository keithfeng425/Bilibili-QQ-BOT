package top.keithfeng;

import love.forte.simboot.spring.autoconfigure.EnableSimbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 程序入口。
 * <p>
 * 当前示例项目中没有添加任何组件依赖，因此可能启动时没有任何bot登录的效果。
 * 你需要按照你的需求添加所需组件，你可以在 <a href="https://github.com/simple-robot">github.com/simple-robot</a> 寻找你需要的组件，例如：
 * <ul>
 * <li> QQ组件：<a href='https://github.com/simple-robot/simbot-component-mirai'>simbot-component-mirai</a> </li>
 * <li> QQ频道组件：<a href='https://github.com/simple-robot/simbot-component-tencent-guild'>simbot-component-tencent-guild</a> </li>
 * <li> Kook组件：<a href='https://github.com/simple-robot/simbot-component-kook'>simbot-component-kook</a> </li>
 * </ul>
 * <p>
 * 默认情况下，在使用 simbot的spring-boot-starter 时你可以不用关心诸如 Component 或 BotManager 的注册，这些行为在 starter 中已经替你做了。
 * <p>
 * 如果你发现下面的 @EnableSimbot 注解是报错的，但是你可以在源代码依赖中找得到此注解（且你使用的是IntelliJ IDEA），
 * 那么你可以考虑：
 * <ul>
 * <li> 1. 更新你的IDEA到更新的版本。(以最新版本为标准) </li>
 * <li> 2. 在插件中<b>禁用</b>Kotlin插件。</li>
 * </ul>
 */
@EnableSimbot // 启用simbot
@EnableScheduling // 启用定时任务
@SpringBootApplication(scanBasePackages = "top.keithfeng")
public class MainApplication {
    /**
     * main方法，启动Spring应用程序。
     */
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
        System.out.println("\n" +
                " __  ___  _______  __  .___________. __    __  \n" +
                "|  |/  / |   ____||  | |           ||  |  |  | \n" +
                "|  '  /  |  |__   |  | `---|  |----`|  |__|  | \n" +
                "|    <   |   __|  |  |     |  |     |   __   | \n" +
                "|  .  \\  |  |____ |  |     |  |     |  |  |  | \n" +
                "|__|\\__\\ |_______||__|     |__|     |__|  |__| \n\n" +
                "=========== Bilibili-QQ-Bot 已启动 ===========");
    }
}

