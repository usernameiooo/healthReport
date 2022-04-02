package com.report.main;

import com.report.config.Environment;
import com.report.config.User;
import com.report.util.TxtHandler;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**配置类</br>
 * 加载、保存、设置和确认配置*/
public class ConfigManager {
    String pathname;

    public ConfigManager(String pathname) {
        this.pathname = pathname;
    }

    Set<User> users=new HashSet<>();
    Environment environment=null;
    public void getConfig(){
        TxtHandler.readLinesAndDoOperation(pathname, new TxtHandler.Operation<String>() {
            String str="";
            @Override
            public void doOperation(String value) {
                if(value.equals("User{")){
                    str=value+"\n";
                }else if(value.equals("}")){
                    if(str.startsWith("User{")){
                        User user=User.parseFromString(str);
                        if(User.check(user))
                        users.add(user);
                    }else if(str.startsWith("Environment{")){
                        Environment env = Environment.parseFromString(str);
                        if(Environment.check(env))environment=env;
                    }
                    str="";
                }else str=str+value+"\n";
            }
        });
    }
    public void saveConfig(){
        if(environment!=null)
        TxtHandler.write(pathname,environment.toString()+"\n",false);
        for(User user:users)
        TxtHandler.write(pathname,user.toString()+"\n",true);
    }
    public void setEnvironment(){
        Environment env=new Environment();
        Scanner scanner=new Scanner(System.in);
        while (true){
        System.out.println("全局设置");
        System.out.println("用于发送消息的qq邮箱？");
        env.setAccount(scanner.nextLine().trim());
        System.out.println("邮箱授权码？");
        env.setAuthCode(scanner.nextLine().trim());
        System.out.println("在0点的第几分钟运行？");
        env.setTime(scanner.nextInt());
        if(!Environment.check(env)){
            System.out.println("设置有误，请重新设置");
        }else {
            this.environment=env;
            break;
        }
        }
        //scanner.close();
        saveConfig();
    }
    public void addUsers(){
        Scanner scanner=new Scanner(System.in);
        while (true){
        User user=new User();
        System.out.println("添加用户");
        System.out.println("姓名？");
        user.setName(scanner.nextLine());
        System.out.println("学号？");
        user.setAccount(scanner.nextLine());
        System.out.println("密码？");
        user.setPassword(scanner.nextLine());
        System.out.println("通知邮箱？");
        user.setMail(scanner.nextLine());
        if(!User.check(user)){
            System.out.println("添加的用户有误，请重新添加");
            continue;
        }
        boolean add = users.add(user);
        if(!add){
            System.out.println("该用户与之前的用户重复，已替换之前的用户");
        }
        saveConfig();
        System.out.println("继续添加？Y/N");
        String continue_ = scanner.nextLine();
        if(!continue_.equalsIgnoreCase("Y"))break;
        }
    }
}
