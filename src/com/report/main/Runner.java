package com.report.main;

import com.report.client.Client;
import com.report.client.MailSender;
import com.report.config.Environment;
import com.report.config.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**运行类<br/>
 * 程序的运行逻辑*/
public class Runner {
    ConfigManager configManager=null;
    Runner(){
        init();
    }
    /**初始化，保证获得可运行的配置*/
    public void init(){
        String property = System.getProperty("user.dir");
        System.out.println("程序所在路径:"+property);
        String pathname=property+"\\健康上报配置.txt";
        System.out.println("配置文件:"+pathname);
        configManager=new ConfigManager(pathname);
        File file=new File(pathname);
        if(file.exists()){
            System.out.println("读取配置文件");
            configManager.getConfig();
            if(!Environment.check(configManager.environment)){
                System.out.println("全局设置为空，需要进行设置");
                configManager.setEnvironment();
            }
            if(configManager.users.size()==0){
                System.out.println("用户列表为空，需要添加用户");
                configManager.addUsers();
            }
        }else {
            System.out.println("未找到配置文件，需要进行设置");
            configManager.setEnvironment();
            configManager.addUsers();
        }
        mailSender=new MailSender(configManager.environment.getAccount(),
                configManager.environment.getAuthCode());
    }
    public void run(){
        runEveryDay(configManager.environment.getTime());
    }
    /**菜单*/
    public void entry(){
        Scanner scanner=new Scanner(System.in);
         while (true){
             System.out.println("想要做什么？\n-g 修改全局设置 \n-a 添加用户 \n-s 开始运行 \n-e 退出");
             String line=scanner.nextLine();
            if(line.equalsIgnoreCase("-g"))configManager.setEnvironment();
            else if(line.equalsIgnoreCase("-a"))configManager.addUsers();
            else if(line.equals("-s"))break;
            else if (line.equals("-e"))System.exit(0);
            else System.out.println("未知命令");
        }
        run();
    }
    public static void main(String[] args) {
        Runner runner=new Runner();
        runner.entry();
    }
    Client client=new Client();
    MailSender mailSender;
    /**为一个用户上报一次并用邮件通知上报结果*/
    public void runOnceForUser(User user){
        String result=null;
        System.out.println("为"+user.getAccount()+
                (user.getName()!=null?"("+user.getName()+")":"")
        +"上报");
        try {
            result= client.reportHealth(user.getAccount(), user.getPassword());
        }catch (Exception e){
            e.printStackTrace();
            result=Client.REPORT_FAIL_START+"上报健康状况失败";
        }
        System.out.println(result);
        if(result.startsWith(Client.REPORT_FAIL_START))result+="，请手动上报";
        mailSender.sendMailInThread(user.getMail(),result,result);
    }
    private static final long PERIOD_DAY = 24*60*60*1000;
    Timer timer=new Timer();
    /**用timer和timer task达到每天定时运行的效果*/
    public void runEveryDay(int min){
        Date date=new Date();
        date.setHours(0);
        date.setMinutes(min);
        date.setSeconds(1);
        if(date.before(new Date()))date.setTime(date.getTime()+PERIOD_DAY);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for(User user: configManager.users){
                    runOnceForUser(user);
                }
                date.setTime(date.getTime()+PERIOD_DAY);
                System.out.println("下次上报将在"+smf(date));
            }
        },date,PERIOD_DAY);
        System.out.println("第一次上报将在"+smf(date));
    }
    private String smf(Date date){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

}
