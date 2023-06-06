# Bilibili-QQ-BOT
自动转发B站用户的动态到QQ群

1. 打开Release，下载最新版本的压缩包

2. 打开`application.yml`文件，将底部的`uid`替换为要订阅的主播uid，`group`修改为机器人推送的QQ群

3. 打开`my-bot.bot.json`，将`code`替换为机器人的QQ号，`passwordInfo -> text`修改为机器人的登录密码（若担心明文不安全，可参考[官方教程](https://component-mirai.simbot.forte.love/docs/bot-config/)改用MD5存储密码）；其余配置选项默认不需更改，若要修改，请同样参照上方的官方教程

4. 解压后在资源管理器顶部的地址栏中输入`CMD`（不区分大小写），或在空白处右键选择`在终端中打开(T)`，然后输入`java -jar simbot-bilibili-1.0-SNAPSHOT.jar`，回车键运行

5. 由于QQ在新设备登录时需要滑动图块和短信进行验证，请参考文章[qq机器人如何滑动验证码验证TxCaptchaHelper](https://blog.csdn.net/dqfe123/article/details/126757443)获取验证token
