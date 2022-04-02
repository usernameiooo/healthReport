package com.report.config;

import com.report.analyse.StringUtil;

/**环境配置</br/>
 * 用来发送消息的邮箱,上报时间*/
public class Environment {
    String account;
    String authCode;//授权码
    int time;

    @Override
    public String toString() {
        return "Environment{" +
                "\naccount='" + account + '\'' +
                "\n, authCode='" + authCode + '\'' +
                "\n, time=" + time +
                "\n}";
    }

    public static Environment parseFromString(String str){
        try {
        Environment e=new Environment();
        e.account=StringUtil.getBetween(str,"account='","'");
        e.authCode=StringUtil.getBetween(str,", authCode='","'");
        e.time= e.parseInt(StringUtil.getBetween(str,", time=","\n"),10);
        if(check(e))
        return e;
        }catch (Exception e){
        }
        return null;
    }
    public static boolean check(Environment e){
        if(e==null)return false;
        if(e.account==null||e.authCode==null)return false;
        if(e.account.isEmpty()||e.authCode.isEmpty())return false;
        if(e.time<=5)e.time=5;
        return true;
    }
    private int parseInt(String str,int def){
        try {
            return Integer.parseInt(str);
        }catch (Exception e){
            return def;
        }
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setTime(int time) {
        if(time<5)time=5;
        this.time = time;
    }

    public String getAccount() {
        return account;
    }

    public int getTime() {
        return time;
    }

    public String getAuthCode() {
        return authCode;
    }
}
