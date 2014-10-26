package me.xiaopan.android.gohttp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.FileEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 本地缓存管理器
 */
public class DefaultCacheManager implements CacheManager{
    private String cacheDirectory;	// 缓存目录

    @Override
    public void saveHttpResponseToCache(HttpRequest request, HttpResponse response) throws IOException {
        HttpEntity httpEntity = response.getEntity();
        if(httpEntity == null){
            if(request.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, createLog(request, "网络 - 缓存失败，原因：Http实体是null"));
            return;
        }

        // 保存状态行
        String cacheId = request.getCacheConfig().getId();
        File statusLineCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".status_line");
        try {
            if(createFile(statusLineCacheFile) == null ){
                if(request.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, createLog(request, "网络 - 创建缓存文件失败"));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        StatusLineCache statusLineCache = new StatusLineCache(response.getStatusLine());
        try {
            writeString(statusLineCacheFile, new Gson().toJson(statusLineCache), false);
        } catch (IOException e) {
            e.printStackTrace();
            statusLineCacheFile.delete();
            return;
        }

        // 保存响应头
        File responseHeadersCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".headers");
        try {
            if(createFile(responseHeadersCacheFile) == null){
                statusLineCacheFile.delete();
                if(request.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, createLog(request, "网络 - 创建缓存文件失败"));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            statusLineCacheFile.delete();
            return;
        }
        Header[] headers = response.getAllHeaders();
        String[] headerStrings = new String[headers.length];
        for(int w = 0; w < headers.length; w++){
            Header header = headers[w];
            if(header instanceof BufferedHeader){
                headerStrings[w] = header.toString();
            }else{
                headers[w] = null;
            }
        }
        try {
            writeString(responseHeadersCacheFile, new Gson().toJson(headerStrings), false);
        } catch (IOException e) {
            e.printStackTrace();
            statusLineCacheFile.delete();
            responseHeadersCacheFile.delete();
            return;
        }

        // 保存响应体
        File responseEntityCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".entity");
        try {
            if(createFile(responseEntityCacheFile) == null){
                statusLineCacheFile.delete();
                responseHeadersCacheFile.delete();
                if(request.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, createLog(request, "网络 - 创建缓存文件失败"));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            statusLineCacheFile.delete();
            responseHeadersCacheFile.delete();
            return;
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try{
            inputStream = httpEntity.getContent();
            fileOutputStream = new FileOutputStream(responseEntityCacheFile);
            outputFromInput(inputStream, fileOutputStream);
            inputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        }catch(IOException exception){
            exception.printStackTrace();

            if(inputStream != null){ try{inputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}
            if(fileOutputStream != null){try{fileOutputStream.flush();fileOutputStream.close();}catch (Exception exception2){exception2.printStackTrace();}}

            statusLineCacheFile.delete();
            responseHeadersCacheFile.delete();
            responseEntityCacheFile.delete();

            if(responseEntityCacheFile.delete() || responseHeadersCacheFile.delete() || statusLineCacheFile.delete()){
                if(request.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, createLog(request, "网络 - 缓存响应失败，缓存文件已刪除"));
            }else{
                if(request.getGoHttp().isDebugMode()) Log.e(GoHttp.LOG_TAG, createLog(request, "网络 - 缓存响应失败，缓存文件刪除失敗"));
            }

            throw exception;
        }

        // 将响应实体替换为本地文件
        Header contentTypeHeader = httpEntity.getContentType();
        response.setEntity(new FileEntity(responseEntityCacheFile,contentTypeHeader != null?contentTypeHeader.getValue():null));
        if(request.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, createLog(request, "网络 - 响应已缓存"));
    }

    @Override
    public boolean isHasAvailableCache(HttpRequest request) {
        // 如果不需要缓存直接返回false
        if(request.getCacheConfig() == null){
            return false;
        }

        // 如果缓存ID为null说明不需要缓存直接返回false
        String cacheId = request.getCacheConfig().getId();
        if(!(cacheId != null && !"".equals(cacheId))){
            return false;
        }

        // 创建缓存文件并根据缓存文件是否存在初步判断缓存是否可用
        File statusLineCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".status_line");
        File responseHeadersCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".headers");
        File responseEntityCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".entity");

        // 如果缓存文件不可用就尝试删除所有的缓存文件并直接返回false
        if(!(statusLineCacheFile.exists() && responseHeadersCacheFile.exists() && responseEntityCacheFile.exists())){
            if(!statusLineCacheFile.delete() && request.getGoHttp().isDebugMode()){
                Log.w(GoHttp.LOG_TAG, createLog(request, "缓存文件删除失败：" + statusLineCacheFile.getPath()));
            }
            if(!responseHeadersCacheFile.delete() && request.getGoHttp().isDebugMode()){
                Log.w(GoHttp.LOG_TAG, createLog(request, "缓存文件删除失败："+responseHeadersCacheFile.getPath()));
            }
            if(!responseEntityCacheFile.delete() && request.getGoHttp().isDebugMode()){
                Log.w(GoHttp.LOG_TAG, createLog(request, "缓存文件删除失败："+responseEntityCacheFile.getPath()));
            }
            if(request.getGoHttp().isDebugMode()) Log.w(GoHttp.LOG_TAG, createLog(request, "缓存 - 文件不存在"));
            return false;
        }

        // 如果有效期小于等于0，就无需验证了，直接返回true
        if(request.getCacheConfig().getPeriodOfValidity() <= 0){
            if(request.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, createLog(request, "缓存 - 永久有效"));
            return true;
        }

        // 根据缓存文件最后修改时间判断是否过期
        long cacheTime = responseEntityCacheFile.lastModified();	// 缓存时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(cacheTime));
        calendar.add(Calendar.MILLISECOND, request.getCacheConfig().getPeriodOfValidity());
        long outOfDateTime = calendar.getTimeInMillis();	// 过期时间
        long currentTime = System.currentTimeMillis();	//当前时间
        boolean isAvailable = outOfDateTime > currentTime;
        if(request.getGoHttp().isDebugMode()){
            if(isAvailable){
                Log.d(GoHttp.LOG_TAG, createLog(request, "缓存 - 有效"));
            }else{
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
                String lastModifiedTimeString = simpleDateFormat.format(new Date(cacheTime));
                String currentTimeString = simpleDateFormat.format(new Date());
                String outOfDateTimeString = simpleDateFormat.format(new Date(outOfDateTime));
                Log.w(GoHttp.LOG_TAG,
                        request.getName()
                                + "缓存 - 已過期"
                                + "，" + "缓存时间：" + lastModifiedTimeString
                                + "；" + "过期时间：" + outOfDateTimeString
                                + "；" + "当前时间：" + currentTimeString
                                + "；" + "缓存有效期：" + request.getCacheConfig().getPeriodOfValidity() + "毫秒"
                                + "（" + request.getUrl() + "）");
            }
        }
        return isAvailable;
    }

    @Override
    public HttpResponse readHttpResponseFromCache(HttpRequest request) {
        if(request.getGoHttp().isDebugMode()) Log.d(GoHttp.LOG_TAG, createLog(request, "本地"));

        // 创建缓存文件
        String cacheId = request.getCacheConfig().getId();
        File statusLineCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".status_line");
        File responseHeadersCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".headers");
        File responseEntityCacheFile = getCacheFile(request.getGoHttp(), cacheId + ".entity");

        // 读取状态行并初始化响应
        StatusLineCache statusLineCache;
        try {
            statusLineCache = new Gson().fromJson(readString(statusLineCacheFile), StatusLineCache.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        HttpResponse httpResponse = new BasicHttpResponse(statusLineCache.toStatusLine());

        // 读取响应头
        String responseHeaders;
        try {
            responseHeaders = readString(responseHeadersCacheFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String[] headerStrings = new Gson().fromJson(responseHeaders, new TypeToken<String[]>(){}.getType());
        if(headerStrings != null && headerStrings.length > 0){
            Header[] headers = new Header[headerStrings.length];
            int w = 0;
            for(String string : headerStrings){
                CharArrayBuffer charArrayBuffer = new CharArrayBuffer(string.length());
                charArrayBuffer.append(string);
                headers[w++] = new BufferedHeader(charArrayBuffer);
            }
            httpResponse.setHeaders(headers);
        }

        // 设置响应体
        Header contentTypeHeader = null;
        Header[] contentTypes = httpResponse.getHeaders(HTTP.CONTENT_TYPE);
        if(contentTypes != null && contentTypes.length > 0){
            contentTypeHeader = contentTypes[0];
        }
        httpResponse.setEntity(new FileEntity(responseEntityCacheFile, contentTypeHeader != null?contentTypeHeader.getValue():null));

        return httpResponse;
    }

    private String createLog(HttpRequest request, String type){
        return request.getName()+" "+type+"（"+request.getUrl()+"）";
    }

    /**
     * 获取缓存文件
     */
    private File getCacheFile(GoHttp goHttp, String fileName){
        if(cacheDirectory != null && !"".equals(cacheDirectory)){
            return new File(cacheDirectory + File.separator + "go_http" + File.separator  + fileName);
        }else{
            return new File(getDynamicCacheDir(goHttp.getContext()).getPath() + File.separator + "easy_http_client" + File.separator  + fileName);
        }
    }

    /**
     * 获取动态获取缓存目录
     * @param context 上下文
     * @return 如果SD卡可用，就返回外部缓存目录，否则返回机身自带缓存目录
     */
    private File getDynamicCacheDir(Context context){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File dir = context.getExternalCacheDir();
            if(dir == null){
                dir = context.getCacheDir();
            }
            return dir;
        }else{
            return context.getCacheDir();
        }
    }

    /**
     * 把给定的字符串写到给定的文件中
     * @param file 给定的文件
     * @param string 给定的字符串
     * @param isAppend 是否追加到文件末尾
     * @throws IOException
     */
    private void writeString(File file, String string, boolean isAppend) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(file, isAppend));
        try {
            bw.write(string);
        }finally {
            try {
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从给定的字节输入流中读取字节再通过给定的字节输出流写出
     * @param input 给定的字节输入流
     * @param output 给定的字节输出流
     * @throws IOException
     */
    private void outputFromInput(InputStream input, OutputStream output) throws IOException{
        byte[] bytes = new byte[1024];
        int number;
        while((number = input.read(bytes)) != -1){
            output.write(bytes, 0, number);
        }
        output.flush();
    }

    /**
     * 从给定的文件中读取字符串
     * @param file 给定的文件
     * @return 字符串
     * @throws IOException
     */
    private String readString(File file) throws IOException {
        Reader reader = new FileReader(file);
        char[] chars = new char[1024];
        CharArrayWriter caw = new CharArrayWriter();
        int number;
        try {
            while((number = reader.read(chars)) != -1){
                caw.write(chars, 0, number);
            }
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String(caw.toCharArray());
    }

    /**
     * 创建文件，此方法的重要之处在于，如果其父目录不存在会先创建其父目录
     * @throws IOException
     */
    private File createFile(File file) throws IOException{
        if(!file.exists()){
            boolean makeSuccess = true;
            File parentFile = file.getParentFile();
            if(!parentFile.exists()){
                makeSuccess = parentFile.mkdirs();
            }
            if(makeSuccess){
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

    public static class StatusLineCache {
        private String reasonPhrase;
        private int statusCode;
        private int major;
        private int minor;
        private String protocol;

        public StatusLineCache(StatusLine statusLine){
            this.reasonPhrase = statusLine.getReasonPhrase();
            this.statusCode = statusLine.getStatusCode();
            ProtocolVersion protocolVersion = statusLine.getProtocolVersion();
            this.major = protocolVersion.getMajor();
            this.minor = protocolVersion.getMinor();
            this.protocol = protocolVersion.getProtocol();
        }

        public StatusLineCache(){

        }

        public StatusLine toStatusLine(){
            return new BasicStatusLine(new ProtocolVersion(protocol, major, minor), statusCode, reasonPhrase);
        }
    }

    @Override
    public void setCacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }
}
