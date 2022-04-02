package com.report.client;


import com.report.multi.AutoRetry;
import com.report.multi.WatchedThread;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**封装网络操作*/
public class SourceCode {
	StringBuffer sourceCode = null;
	public SourceCode() {
	    init();
	}
	/**打印响应头*/
	private void getHeaders(HttpURLConnection coon){
		Map<String, List<String>> headerFields = coon.getHeaderFields();
		Set<String> strings = headerFields.keySet();
	    for(String key:strings){
			System.out.println(key+"="+headerFields.get(key));
		}
	}
	/**发送GET请求<br/>
	 * 请求参数是直接放在url里的*/
	public StringBuffer getSourceCodeInGet(String url,boolean canJump) {
		try {
			StringBuffer sourceCode = new StringBuffer();
			HttpURLConnection httpUrlCon;
			httpUrlCon = getConnection(url);
			httpUrlCon.setRequestMethod("GET");
			httpUrlCon.setDoOutput(false);
			handleHeader(httpUrlCon);
			//Headers(httpUrlCon);
			int responseCode=httpUrlCon.getResponseCode();
			switch (responseCode){
				case 302: case 301:
					if(!canJump)return null;
					System.out.println("跳转");
					//跳转
					String newUrl=httpUrlCon.getHeaderField("Location");
			        if(newUrl==null)newUrl=httpUrlCon.getHeaderField("location");
					newUrl=getFullUrl(url,newUrl);
					if(newUrl==null)return null;
					return getSourceCodeInGet(newUrl,canJump);
				case 404: case 403:return null;
				case 200: return getContent(httpUrlCon);
			}
		} catch (IOException e) {
			System.err.println(url);
			e.printStackTrace();
			//userAgent=(userAgent+1)%userAgents.length;
		    //出现其他异常则返回
		}
		return null;
	}
	/**获取全路径<br/>
	 * 根据站点和想对路径*/
	public String getFullUrl(String sourceUrl,String path){
		try {
		int protocolIndex = sourceUrl.indexOf("//")+2;
	    int siteIndex=sourceUrl.indexOf("/",protocolIndex);
		String site=sourceUrl.substring(0,siteIndex);
		return site+path;
		}catch (Exception e){}
		return null;
	}
	/**发送POST请求
	 * @param data 请求的参数，是形同get请求的url里&后的参数部分*/
	public StringBuffer getSourceCodeInPost(String url,String data){
		StringBuffer sourceCode = new StringBuffer();
		HttpURLConnection httpUrlCon;
		httpUrlCon = getConnection(url);
		PrintWriter printWriter = null;
		//关键代码 application/x-www-form-urlencoded
		try {
			httpUrlCon.setRequestMethod("POST");
			httpUrlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			//输出表单数据
			printWriter = new PrintWriter(httpUrlCon.getOutputStream());
			printWriter.write(data);
			printWriter.flush();
		    printWriter.close();
		    //处理响应头
            handleHeader(httpUrlCon);
			int responseCode=httpUrlCon.getResponseCode();
			switch (responseCode){
				case 302: case 301:
					//跳转
					String newUrl=httpUrlCon.getHeaderField("location");
					if(newUrl==null)newUrl=httpUrlCon.getHeaderField("Location");
					getSourceCodeInGet(newUrl,true);
					break;
				case 404: case 403:return null;
				case 200: return getContent(httpUrlCon);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**响应正文的编码*/
	Charset charset=StandardCharsets.UTF_8;
	/**获取响应的正文内容*/
    public StringBuffer getContent(HttpURLConnection connection) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
		String line;
		StringBuffer sourceCode=new StringBuffer();
		while ((line = reader.readLine()) != null) {
			sourceCode.append(line).append("\n");
		}
		reader.close();
		this.sourceCode=sourceCode;
		return sourceCode;
	}
	/**对响应头的处理<br/>
	 * 更新cookie*/
	private void handleHeader(HttpURLConnection connection){
		addCookie(connection.getHeaderField("Set-Cookie"));
	}
	 int userAgent=2;
	/**供选择的客户端*/
	static String[] userAgents={"Chrome/71.0.3578.98 Safari/537.36",
			"Mozilla/4.0 compatible; MSIE 5.0;Windows NT;",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"};
	/**根据url获取连接*/
	public HttpURLConnection getConnection(String urlStr) {
		URL url;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("user-agent",userAgents[userAgent]);
			conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			conn.setRequestProperty("accept-language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			//conn.setRequestProperty("accept-encoding", "gzip, deflate, br");
			conn.setRequestProperty("cache-control","max-age=0");
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("cookie",getCookieString());
			conn.setRequestProperty("upgrade-insecure-requests", "1");
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setReadTimeout(60*1000);
			conn.setConnectTimeout(60*1000);
			if(conn instanceof HttpsURLConnection){
				((HttpsURLConnection)conn).setSSLSocketFactory(getSSLFactory());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		setRequestHeader(conn,initHeader);
		initHeader=null;
		return conn;
	}
	/**第一次连接的初始化请求头*/
	String initHeader=null;
	/**设置请求头
	 * @param text 浏览器开发者模式的请求头，各参数以\n分隔*/
	public void setRequestHeader(HttpURLConnection connection,String text){
		if(text==null)return;
		String[] lines = text.split("\n");
	    for(String line:lines){
			if(line.startsWith(":"))line=line.substring(1);
			String[] split = line.split(":");
		    if(split.length!=2)continue;
			connection.setRequestProperty(split[0].trim(),split[1].trim());
		}
	}
	public void setInitHeader(String initHeader) {
		this.initHeader = initHeader;
	}
    /**cookie列表*/
	Map<String,String>cookieMap=new HashMap<>();
	/**将响应的cookie放入列表*/
	public void addCookie(String set_cookie){
		if(set_cookie==null)return;
		 String[] cookies=set_cookie.split(";");
	     for(String c:cookies){
	     	if(c.contains("=")) {
				String[] split = c.split("=");
				if(!split[0].trim().equals("path"))
			    cookieMap.put(split[0].trim(),split[1].trim());
	     	}
		 }
	}
	/**https连接的证书*/
	public SSLSocketFactory getSSLFactory(){
		try {
		SSLContext sslContext=SSLContext.getInstance("SSL");
		TrustManager[] tm={new X509TrustManager(){

			@Override
			public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

			}

			@Override
			public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		}};
		//初始化
		sslContext.init(null, tm, new java.security.SecureRandom());
		//获取SSLSocketFactory对象
		SSLSocketFactory ssf=sslContext.getSocketFactory();
		return ssf;
		}catch ( NoSuchAlgorithmException | KeyManagementException e){

		}
		return null;
	}
	/**将cookie列表提取为请求头的cookie字段的值</br>
	 * key=value;的形式*/
	public String getCookieString(){
		StringBuilder cookie= new StringBuilder();
		for(String key: cookieMap.keySet()){
			cookie.append(key).append("=").append(cookieMap.get(key)).append(";");
		}
		//System.out.println(cookie.toString());
		return cookie.toString();
	}
	private void init() {

	}
	public void clearCookies(){
		cookieMap.clear();
	}
	/**超时监控、自动重试地发送get请求，直到获得正确响应
	 * @param time 最大尝试次数*/
    public String getHtmlWithAutoRetry(String url,int time){
		AutoRetry<String> autoRetry=new AutoRetry<String>() {
			@Override
			public WatchedThread<String> getNewThread() {
				return new WatchedThread<String>() {
					@Override
					public void callBack(String result) {
					}
					@Override
					public String runTask() {
						StringBuffer sourceCodeInGet = getSourceCodeInGet(url,true);
						if(sourceCodeInGet==null)return null;
						return sourceCodeInGet.toString();
					}
				};
			}
		};
		autoRetry.setRetryTimes(time);
		autoRetry.setRetrySpace(5000);
		return autoRetry.runForUsableResult();
	}
	int retryTime=3;
	/**超时监控、自动重试地发送get请求，直到获得正确响应*/
	public String getHtmlWithAutoRetry(String url){
		return getHtmlWithAutoRetry(url,retryTime);
	}

	public void setRetryTime(int retryTime) {
		this.retryTime = retryTime;
	}
}
