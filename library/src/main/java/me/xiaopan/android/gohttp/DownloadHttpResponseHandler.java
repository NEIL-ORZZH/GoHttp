package me.xiaopan.android.gohttp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;

import android.os.Handler;

/**
 * 下载Http响应处理器
 */
public abstract class DownloadHttpResponseHandler extends HttpResponseHandler{
    private File file;
    
	public DownloadHttpResponseHandler(File file, boolean enableProgressCallback) {
		super(enableProgressCallback);
		if(file == null){
    		throw new IllegalArgumentException("file 参数不能为null");
    	}
        this.file = file;
	}

    protected DownloadHttpResponseHandler(File file){
    	if(file == null){
    		throw new IllegalArgumentException("file 参数不能为null");
    	}
        this.file = file;
    }

	@Override
	public DownloadHttpResponseHandler setEnableProgressCallback(boolean enableProgressCallback) {
		super.setEnableProgressCallback(enableProgressCallback);
		return this;
	}

	@Override
	public DownloadHttpResponseHandler setSynchronizationCallback(boolean synchronizationCallback) {
		super.setSynchronizationCallback(synchronizationCallback);
		return this;
	}

	@Override
    protected final void onStart(Handler handler) {
    	if(isCancelled()) return;
    	
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(isCancelled()) return;
    				onStart();
    			}
    		});
    	}else{
			onStart();
    	}
    }

    @Override
    protected final void onHandleResponse(Handler handler, HttpUriRequest request, HttpResponse httpResponse, boolean isNotRefresh, boolean isOver) throws Throwable {
    	if(!(httpResponse.getStatusLine().getStatusCode() > 100 && httpResponse.getStatusLine().getStatusCode() < 300)){
			if(httpResponse.getStatusLine().getStatusCode() == 404){
				throw new FileNotFoundException("请求地址错误："+request.getURI().toString());
			}else{
				throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), "异常状态码："+httpResponse.getStatusLine().getStatusCode()+"："+request.getURI().toString());
			}
		}
		
		HttpEntity httpEntity = httpResponse.getEntity();
		if(httpEntity == null){
            throw new Exception("没有响应体："+request.getURI().toString());
		}
		
		if(createFile(file) == null){
			throw new IllegalArgumentException("创建文件失败："+file.getPath());
		}
		
		OutputStream outputStream = null;
		try{
			outputStream = new BufferedOutputStream(new FileOutputStream(file), 8*1024);
			BaseUpdateProgressCallback baseUpdateProgressCallback = isEnableProgressCallback()?new BaseUpdateProgressCallback(this, handler):null;
			ProgressEntityUtils.read(httpEntity, outputStream, this, baseUpdateProgressCallback);
		}finally{
			GeneralUtils.close(outputStream);
		}
		if(isCancelled()){
			if(file.exists()){
				file.delete();
			}
			return;
		}
		
		if(!isSynchronizationCallback()){
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(isCancelled()) return;
					onSuccess(file);
				}
			});
		}else{
			onSuccess(file);
		}
    }
    
    @Override
    protected final void onUpdateProgress(Handler handler, final long totalLength, final long completedLength){
    	if(isCancelled()) return;
    	
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(isCancelled()) return;
    				onUpdateProgress(totalLength, completedLength);
    			}
    		});
    	}else{
    		onUpdateProgress(totalLength, completedLength);
    	}
    }
    
    @Override
    protected final void onException(Handler handler, final Throwable e, boolean isNotRefresh) {
    	if(isCancelled()) return;
    	
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				if(isCancelled()) return;
    				onFailure(e);
    			}
    		});
    	}else{
    		onFailure(e);
    	}
    }

    @Override
    protected final void onCancel(Handler handler) {
    	if(!isSynchronizationCallback()){
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				onCancel();
    			}
    		});
    	}else{
    		onCancel();
    	}
    }

    /**
     * 请求开始
     */
    protected abstract void onStart();

    /**
     * 更新进度
     * @param totalLength 总长度
     * @param completedLength 已完成长度
     */
    public void onUpdateProgress(long totalLength, long completedLength){}

    /**
     * 请求成功
     * @param file  保存数据的文件
     */
    public abstract void onSuccess(File file);

    /**
     * 请求失败
     * @param throwable 异常
     */
    public abstract void onFailure(Throwable throwable);

    /**
     * 请求取消
     */
    protected void onCancel(){

    }
    
    /**
	 * 创建文件，此方法的重要之处在于，如果其父目录不存在会先创建其父目录
	 * @throws IOException
	 */
	private static File createFile(File file) throws IOException{
		if(!file.exists()){
			boolean createSuccess = true;
			File parentFile = file.getParentFile();
			if(!parentFile.exists()){
				createSuccess = parentFile.mkdirs();
			}
			if(createSuccess){
				try{
					file.createNewFile();
					return file;
				}catch(IOException exception){
					exception.printStackTrace();
					return null;
				}
			}else{
				return null;
			}
		}else{
			return file;
		}
	}
}
