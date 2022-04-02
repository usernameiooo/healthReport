# healthReport
自动进行某高校防疫平台的健康上报。仅作为辅助使用。如有异常情况，需要如实上报<br/>
在电脑上运行。使用URLConnection进行网络操作，登录平台进行健康上报；通过QQ邮箱发送上报结果。使用Timer进行定时任务，实现每天自动上报。<br/>
运行方式
 - 1.生成jar包
 - 运行jar包，进行第一次使用的配置。会在jar包所在目录下生成配置文件
 - 后续运行无需再进行配置 
 
 
 建议创建bat文件，将bat文件和jar包放在同目录下（桌面创建bat文件的快捷方式），可以通过双击bat文件运行jar包。<br/>
 bat文件内容
 ```bash
    cd  %~dp0
    java -Dfile.encoding=utf-8 -jar healthReport.jar
    pause
 ```
注意要使jar包运行才能自动进行健康上报。电脑重启或程序关闭会导致不能自动进行健康上报
<br/> 
 配置后运行效果
 ![image](https://user-images.githubusercontent.com/74187392/161384915-734dfb34-4320-49cd-8a65-8b83feb296d2.png)
android版 https://github.com/usernameiooo/healthReport-android
