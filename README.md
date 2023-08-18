# Bilibili-QQ-BOT
自动转发B站用户的动态及直播开播下播提醒到QQ群

1. 打开Release，下载最新版本的压缩包

2. 打开`application.yml`文件，将底部的`uid`替换为要订阅的主播uid，`group`修改为机器人推送的QQ群，`room`修改为订阅的直播间号码，并在`notify-all`下配置抓取到UP新动态及直播开播/下播时是否要@全体成员（true为开启@全体成员，false为关闭@全体成员）

3. 如果需要启用机器人聊天功能，请先申请[OpenID](https://platform.openai.com/)或[API2D](https://api2d.com/wiki/doc)的API Key，填写在`application.yml`文件底部的`api-key`中，如果使用API2D作代理的话还需将上方`use-api2d`改为`true`，若网络环境可以直接打开ChatGPT则该选项置为`false`；由于GPT生成回答时间较长，当答案文本过长时会超时，所以请尽量控制提问时限制回答的字数（该功能仅用于学习研究，有被QQ封禁的危险，使用时还请注意）

4. 打开`simbot-bots\my-bot.bot.json`，将`code`替换为机器人的QQ号，`passwordInfo -> text`修改为机器人的登录密码（若担心明文不安全，可参考[官方教程](https://component-mirai.simbot.forte.love/docs/bot-config/)改用MD5存储密码）；其余配置选项默认不需更改，若要修改，请同样参照上方的官方教程

5. 解压后在资源管理器顶部的地址栏中输入`CMD`（不区分大小写），或在空白处右键选择`在终端中打开(T)`，然后输入`unidbg-fetch-qsign-1.1.7\bin\unidbg-fetch-qsign.bat --basePath=unidbg-fetch-qsign-1.1.7\txlib\8.9.63`，不要关闭该窗口

6. 在资源管理器顶部的地址栏中输入`CMD`（不区分大小写），或在空白处右键选择`在终端中打开(T)`，打开新的命令行/终端，输入`java -jar simbot-bilibili-2.0.jar`，回车键运行

7. 由于QQ在新设备登录时需要滑动图块和短信进行验证，请参考文章[qq机器人如何滑动验证码验证TxCaptchaHelper](https://blog.csdn.net/dqfe123/article/details/126757443)获取验证token

#### 常见问题：
1. 控制台出现大量报错，重启程序后提示QQ版本过低，需要升级？

  出现此类提示说明当前使用的QQ号被tx列入风险管控名单了，tx一直在打压各类QQ机器人，可以参考上方第5步部署本地第三方签名服务，即可正常使用

2. 设置了直播间订阅，但是没有发送开播和下播通知？

  请在群里为机器人设置管理员身份，因为开播和下播通知需要使用`@全体成员`功能

#### 写给开发人员：
1. 由于上方常见问题1所述，目前需要部署第三方签名服务才可正常实现QQ机器人的登录，需要借助临时性协议修复插件`fix-protocol-version`，但该插件未登陆Maven，所以开发前需要手动执行以下代码导入到本地Maven库：
```shell
mvn install:install-file -Dfile=fix-protocol-version-1.10.0.mirai2.jar -DgroupId=cssxsh -DartifactId=fix-protocol-version -Dversion=1.10.0.mirai2 -Dpackaging=jar
```

#### 感谢开源项目：
1. 临时性协议修复插件 [cssxsh / fix-protocol-version](https://github.com/cssxsh/fix-protocol-version)

2. 第三方签名服务 [fuqiuluo / unidbg-fetch-qsign](https://github.com/fuqiuluo/unidbg-fetch-qsign)

##### 有其他任何问题或功能方面的改进意见，欢迎提交Issues联系作者
