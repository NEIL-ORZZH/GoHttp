package me.xiaopan.android.gohttp.sample.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Failure {
	private static final int CODE_EXCEPTION = 123142;	//错误码 - 异常

	private int code;
	private String message;
	
	private Failure(int code, String message){
		setCode(code);
		setMessage(message);
	}
	
	public Failure(){
		
	}
	
	/**
	 * 获取错误码
	 * @return 错误码
	 */
	public int getCode() {
		return code;
	}
	
	/**
	 * 设置错误码
	 * @param code 错误码
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/**
	 * 获取错误提示消息
	 * @return 错误提示消息
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * 设置错误提示消息
	 * @param message 错误提示消息
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString(){
		return "code="+getCode()+"; message="+getMessage();
	}
	
	public boolean isException(){
		return code == CODE_EXCEPTION;
	}
	
	/**
	 * 101：没有在Mainifest中添加INTERNET权限
	 * 202：主机地址错误或应用被禁止联网
	 * 404：请求地址错误
	 * 909：未知异常
	 * @param context
	 * @param throwable
	 * @return
	 */
	public static Failure buildByException(Context context, Throwable throwable){
		if(throwable instanceof SecurityException){
			return Failure.build(CODE_EXCEPTION, "网络连接异常【101】");
		}else if(throwable instanceof UnknownHostException){
			if(isConnectedByState(context)){
				return Failure.build(CODE_EXCEPTION, "网络连接异常【202】");
			}else{
				return Failure.build(CODE_EXCEPTION, "没有网络连接");
			}
		}else if(throwable instanceof HttpHostConnectException && throwable.getMessage() != null && throwable.getMessage().contains("refused")){
			return Failure.build(CODE_EXCEPTION, "网络连接异常【202】");
		}else if(throwable instanceof SocketTimeoutException || throwable instanceof ConnectTimeoutException){
			return Failure.build(CODE_EXCEPTION, "网络连接超时");
		}else if(throwable instanceof FileNotFoundException){
			return Failure.build(CODE_EXCEPTION, "网络连接异常【404】");
		}else{
			return Failure.build(CODE_EXCEPTION, "网络连接异常【909】"); 
		}
	}
	
	public static Failure build(String message){
		return new Failure(-1212, message);
	}
	
	public static Failure build(int code, String message){
		return new Failure(code, message);
	}
	
	public static boolean isConnectedByState(Context context){
	    NetworkInfo networkInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	    return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
	}
}