/*
 * Copyright (C) 2013 Peng fei Pan <sky@xiaopan.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.android.gohttp;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.Environment;

class GeneralUtils {

    /**
     * 关闭流
     * @param stream 要关闭的流
     */
    public static void close(Closeable stream) {
        if(stream != null){
            if(stream instanceof OutputStream){
                try {
                    ((OutputStream) stream).flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	/**
	 * 判断给定的字符串是否为null或者是空的
	 */
	static boolean isEmpty(String string){
		return string == null || "".equals(string.trim());
	}
	
	/**
	 * 判断给定的字符串是否不为null且不为空
	 */
	static boolean isNotEmpty(String string){
		return !isEmpty(string);
	}
	
	/**
	 * 判断给定的字符串数组中的所有字符串是否都为null或者是空的
	 */
	static boolean isEmpty(String... strings){
		boolean result = true;
		for(String string : strings){
			if(isNotEmpty(string)){
				result = false;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 判断给定的字符串数组中是否全部都不为null且不为空
	 * @param strings 给定的字符串数组
	 * @return 是否全部都不为null且不为空
	 */
	static boolean isNotEmpty(String... strings){
		boolean result = true;
		for(String string : strings){
			if(isEmpty(string)){
				result = false;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 将给定的字符串MD5加密
	 * @param string 给定的字符串
	 * @return MD5加密后生成的字符串
	 */
	static String MD5(String string) {  
		String result = null;
		try {
			char[] charArray = string.toCharArray();
			byte[] byteArray = new byte[charArray.length];
			for (int i = 0; i < charArray.length; i++){
				byteArray[i] = (byte) charArray[i];
			}

			StringBuilder hexValue = new StringBuilder();
			for (byte by : MessageDigest.getInstance("MD5").digest(byteArray)) {
				int val = ((int) by) & 0xff;
				if (val < 16){
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(val));
			}
			
			result = hexValue.toString();  
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	 }
	
	/**
	 * SD卡是否可用
	 * @return 只有当SD卡已经安装并且准备好了才返回true
	 */
	static boolean isSdCardAvailable(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 获取动态获取缓存目录
	 * @param context 上下文
	 * @return 如果SD卡可用，就返回外部缓存目录，否则返回机身自带缓存目录
	 */
	static File getDynamicCacheDir(Context context){
		if(isSdCardAvailable()){
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
	 * 根据默认的格式（yyyy-MM-dd hh:mm:ss）获取当前的日期时间（24小时制）
	 * @return 当前的日期时间
	 */
	static String getCurrentDateTimeBy24Hour(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
	}
	
	/**
	 * 从给定的字符输入流中读取字符
	 * @param reader 给定的字符输入流
	 * @return 全部数据
	 * @throws IOException 
	 */
	static char[] read(Reader reader) throws IOException{
		char[] chars = new char[1024];
		CharArrayWriter caw = new CharArrayWriter();
		int number;
		while((number = reader.read(chars)) != -1){
			caw.write(chars, 0, number);
		}
		return caw.toCharArray();
	}
	
	/**
	 * 打开一个字符输入流
	 * @param file 源文件
	 * @return 字符输入流
	 * @throws FileNotFoundException 
	 */
	static Reader openReader(File file) throws FileNotFoundException{
		return new FileReader(file);
	}
	
	/**
	 * 从给定的文件中读取字符
	 * @param file 给定的文件
	 * @return 全部字符
	 * @throws IOException 
	 */
	static char[] readChar(File file) throws IOException{	
		return read(openReader(file));
	}
	
	/**
	 * 从给定的文件中读取字符串
	 * @param file 给定的文件
	 * @return 字符串
	 * @throws IOException 
	 */
	static String readString(File file) throws IOException{
		return new String(readChar(file));
	}
	
	/**
	 * 创建文件，此方法的重要之处在于，如果其父目录不存在会先创建其父目录
	 * @throws IOException
	 */
	static File createFile(File file) throws IOException{
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
	
	/**
	 * 打开一个字符输出流
	 * @param file 源文件
	 * @param isAppend 是否追加到文件末尾
	 * @return 字符输出流
	 * @throws IOException 
	 */
	static Writer openWriter(File file, boolean isAppend) throws IOException{
		return new FileWriter(file, isAppend);
	}
	
	/**
	 * 打开一个加了缓冲区的字符输出流
	 * @param file 源文件
	 * @param isAppend 是否追加到文件末尾
	 * @return 加了缓冲区的字符输出流
	 * @throws FileNotFoundException 
	 */
	static BufferedWriter openBufferedWriter(File file, boolean isAppend) throws IOException{
		return new BufferedWriter(openWriter(file, isAppend));
	}
	
	/**
	 * 把给定的字符串写到给定的文件中
	 * @param file 给定的文件
	 * @param string 给定的字符串
	 * @param isAppend 是否追加到文件末尾
	 * @throws IOException
	 */
	static void writeString(File file, String string, boolean isAppend) throws IOException{
		BufferedWriter bw = openBufferedWriter(file, isAppend);
		bw.write(string);
		bw.flush();
		bw.close();
	}
	
	/**
	 * 从给定的字节输入流中读取字节再通过给定的字节输出流写出
	 * @param input 给定的字节输入流
	 * @param output 给定的字节输出流
	 * @throws IOException
	 */
	static void outputFromInput(InputStream input, OutputStream output) throws IOException{
		byte[] bytes = new byte[1024];
		int number;
		while((number = input.read(bytes)) != -1){
			output.write(bytes, 0, number);
		}
		output.flush();
	}
	
	/**
	 * 获取一个枚举上的指定类型的注解
	 * @param enumObject 给定的枚举
	 * @param annotation 指定类型的注解
	 */
	static <T extends Annotation> T getAnnotationFromEnum(Enum<?> enumObject, Class<T> annotation){
		try {
			return (T) enumObject.getClass().getField(enumObject.name()).getAnnotation(annotation);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	/* ************************************************** 父类相关的方法 ******************************************************* */
	/**
	 * 获取给定的类所有的父类
	 * @param sourceClass 给定的类
	 * @param isAddCurrentClass 是否将当年类放在最终返回的父类列表的首位
	 * @return 给定的类所有的父类
	 */
	static List<Class<?>> getSuperClasses(Class<?> sourceClass, boolean isAddCurrentClass){
		List<Class<?>> classList = new ArrayList<Class<?>>();
		Class<?> currentClass;
		if(isAddCurrentClass){
			currentClass = sourceClass;
		}else{
			currentClass = sourceClass.getSuperclass();
		}
		while(currentClass != null){
			classList.add(currentClass);
			currentClass = currentClass.getSuperclass();
		}
		return classList;
	}
	
	/**
	 * 获取给定类的所有字段
	 * @param sourceClass 给定的类
	 * @param isGetDeclaredField 是否需要获取Declared字段
	 * @param isFromSuperClassGet 是否需要把其父类中的字段也取出
	 * @param isDESCGet 在最终获取的列表里，父类的字段是否需要排在子类的前面。只有需要把其父类中的字段也取出时此参数才有效
	 * @return 给定类的所有字段
	 */
	static List<Field> getFields(Class<?> sourceClass, boolean isGetDeclaredField, boolean isFromSuperClassGet, boolean isDESCGet){
		List<Field> fieldList = new ArrayList<Field>();
		//如果需要从父类中获取
		if(isFromSuperClassGet){
			//获取当前类的所有父类
			List<Class<?>> classList = getSuperClasses(sourceClass, true);
			
			//如果是降序获取
			if(isDESCGet){
				for(int w = classList.size()-1; w > -1; w--){
					for(Field field : isGetDeclaredField ? classList.get(w).getDeclaredFields() : classList.get(w).getFields()){
						fieldList.add(field);
					}
				}
			}else{
				for(int w = 0; w < classList.size(); w++){
					for(Field field : isGetDeclaredField ? classList.get(w).getDeclaredFields() : classList.get(w).getFields()){
						fieldList.add(field);
					}
				}
			}
		}else{
			for(Field field : isGetDeclaredField ? sourceClass.getDeclaredFields() : sourceClass.getFields()){
				fieldList.add(field);
			}
		}
		return fieldList;
	}
	
	/**
	 * 判断给定字段是否是type类型的数组
	 */
	static boolean isArrayByType(Field field, Class<?> type){
		Class<?> fieldType = field.getType();
		return fieldType.isArray() && type.isAssignableFrom(fieldType.getComponentType());
	}
	
	/**
	 * 判断给定字段是否是type类型的collectionType集合，例如collectionType=List.class，type=Date.class就是要判断给定字段是否是Date类型的List
	 */
	@SuppressWarnings("rawtypes")
	static boolean isCollectionByType(Field field, Class<? extends Collection> collectionType, Class<?> type){
		Class<?> fieldType = field.getType();
		if(collectionType.isAssignableFrom(fieldType)){
			Class<?> first = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
			return type.isAssignableFrom(first);
		}else{
			return false;
		}
	}
	
	static String createCacheId(CacheConfig cacheConfig, String baseUrl, RequestParams requestParams, List<String> cacheIgnoreParams){
		if(cacheConfig != null){
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(baseUrl);
			if(requestParams != null){
				for(BasicNameValuePair basicNameValuePair : requestParams.getParamsList()){
					if(cacheIgnoreParams == null || !cacheIgnoreParams.contains(basicNameValuePair.getName())){
						stringBuilder.append(basicNameValuePair.getName());
						stringBuilder.append(basicNameValuePair.getValue());
					}
				}
			}
			return GeneralUtils.MD5(stringBuilder.toString());
		}else{
			return null;
		}
	}
}
