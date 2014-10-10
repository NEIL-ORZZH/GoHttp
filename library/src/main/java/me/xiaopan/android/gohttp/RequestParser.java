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

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.xiaopan.android.gohttp.annotation.CacheIgnore;
import me.xiaopan.android.gohttp.annotation.False;
import me.xiaopan.android.gohttp.annotation.Header;
import me.xiaopan.android.gohttp.annotation.Host;
import me.xiaopan.android.gohttp.annotation.Method;
import me.xiaopan.android.gohttp.annotation.Name;
import me.xiaopan.android.gohttp.annotation.Param;
import me.xiaopan.android.gohttp.annotation.Path;
import me.xiaopan.android.gohttp.annotation.ResponseBody;
import me.xiaopan.android.gohttp.annotation.True;
import me.xiaopan.android.gohttp.annotation.URL;
import me.xiaopan.android.gohttp.annotation.Value;
import me.xiaopan.android.gohttp.enums.MethodType;
import android.content.Context;

/**
 * 请求解析器，用于解析继承于Request的请求对象
 */
public class RequestParser {
	private static final String DEFAULT_VALUE_TRUE = "true";
	private static final String DEFAULT_VALUE_FALSE = "false";
    
    /**
     * 解析基本的请求地址，值得注意的是此方法返回的请求地址不包含请求参数（即使请求方式是GET或者DELETE）
     * @param context 上下文
     * @param requestClass 请求对象的class
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseBaseUrl(Context context, Class<? extends Request> requestClass){
        StringBuilder stringBuilder = new StringBuilder();
    	
        /* 优先解析Url注解 */
        String url = parseURLAnnotation(context, requestClass);
        if(GeneralUtils.isNotEmpty(url)){
        	stringBuilder.append(url);
        }else{
        	/* 其次解析Host注解 */
        	if(stringBuilder.length() == 0){
        		String host = parseHostAnnotation(context, requestClass);
        		if(GeneralUtils.isNotEmpty(host)){
        			stringBuilder.append(host);
        		}
        	}
        	
        	/* 最后解析Path注解 */
        	if(stringBuilder.length() > 0){
        		String path = parsePathAnnotation(context, requestClass);
        		if(GeneralUtils.isNotEmpty(path)){
        			stringBuilder.append("/");
        			stringBuilder.append(path);
        		}
        	}
        }
        
        if(stringBuilder.length() > 0){
        	return stringBuilder.toString();
    	}else{
    		return null;
    	}
    }

    /**
     * 解析完整的请求地址，所谓完整就是如果是Get请求的话会加上参数
     * @param context 上下文
     * @param requestClass 请求对象的class
     * @param extraRequestParams 额外的请求参数，如果此请求对象的请求方式是GET或者DELETE的话，此参数集就会被处理
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseCompleteUrl(Context context, Class<? extends Request> requestClass, RequestParams extraRequestParams){
        StringBuilder stringBuilder = new StringBuilder();
    	
        /* 优先解析Url注解 */
        String url = parseURLAnnotation(context, requestClass);
        if(GeneralUtils.isNotEmpty(url)){
        	stringBuilder.append(url);
        }else{
        	/* 其次解析Host注解 */
        	if(stringBuilder.length() == 0){
        		String host = parseHostAnnotation(context, requestClass);
        		if(GeneralUtils.isNotEmpty(host)){
        			stringBuilder.append(host);
        		}
        	}
        	
        	/* 最后解析Path注解 */
        	if(stringBuilder.length() > 0){
        		String path = parsePathAnnotation(context, requestClass);
        		if(GeneralUtils.isNotEmpty(path)){
        			stringBuilder.append("/");
        			stringBuilder.append(path);
        		}
        	}
        }
        
        if(stringBuilder.length() > 0){
        	Method method = requestClass.getAnnotation(Method.class);
        	if(method == null || method.value() == MethodType.GET || method.value() == MethodType.DELETE){
        		RequestParams newParams = parseRequestParams(context, requestClass, extraRequestParams);
        		String paramString = newParams.getParamString();
        		if(GeneralUtils.isNotEmpty(paramString)){
        			stringBuilder.append(stringBuilder.indexOf("?") == -1?"?":"&");
        			stringBuilder.append(paramString);
        		}
    		}
        	return stringBuilder.toString();
    	}else{
    		return null;
    	}
    }

    /**
     * 解析请求地址，值得注意的是只有此请求对象中静态字段才会被处理
     * @param context 上下文
     * @param requestClass 请求对象的class
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseCompleteUrl(Context context, Class<? extends Request> requestClass){
    	return parseCompleteUrl(context, requestClass, null);
    }

    /**
     * 解析完整的请求地址，所谓完整就是如果是Get请求的话会加上参数
     * @param context 上下文
     * @param request 请求对象
     * @param extraRequestParams 额外的请求参数，如果此请求对象的请求方式是GET或者DELETE的话，此参数集就会被处理
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseCompleteUrl(Context context, Request request, RequestParams extraRequestParams){
    	StringBuilder stringBuilder = new StringBuilder();
    	
        /* 优先解析Url注解 */
        String url = parseURLAnnotation(context, request.getClass());
        if(GeneralUtils.isNotEmpty(url)){
        	stringBuilder.append(url);
        }else{
        	/* 其次解析Host注解 */
    		String host = parseHostAnnotation(context, request.getClass());
    		if(GeneralUtils.isNotEmpty(host)){
    			stringBuilder.append(host);
    		}
        	
        	/* 最后解析Path注解 */
        	if(stringBuilder.length() > 0){
        		String path = parsePathAnnotation(context, request.getClass());
        		if(GeneralUtils.isNotEmpty(path)){
        			stringBuilder.append("/");
        			stringBuilder.append(path);
        		}
        	}
        }
        
        if(stringBuilder.length() > 0){
        	Method method = request.getClass().getAnnotation(Method.class);
        	if(method == null || method.value() == MethodType.GET || method.value() == MethodType.DELETE){
        		RequestParams newParams = parseRequestParams(context, request, extraRequestParams);
        		String paramString = newParams.getParamString();
        		if(GeneralUtils.isNotEmpty(paramString)){
        			stringBuilder.append(stringBuilder.indexOf("?") == -1?"?":"&");
        			stringBuilder.append(paramString);
        		}
    		}
        	return stringBuilder.toString();
    	}else{
    		return null;
    	}
    }

    /**
     * 解析完整的请求地址，所谓完整就是如果是Get请求的话会加上参数
     * @param context 上下文
     * @param request 请求对象
     * @return 请求地址，null：requestClass为null或请求对象上既没有Url注解（或者值为空）也没有Host注解（或者值为空）
     */
    public static String parseCompleteUrl(Context context, Request request){
    	return parseCompleteUrl(context, request, null);
    }

    /**
     * 解析请求参数集
     * @param context 上下文
     * @param requestClass 请求对象的class，一定要注意，从class上解析请求参数的时候只解析静态的字段
     * @param requestParams 请求参数集，如果不为null的话，会将解析到的请求参数放入此请求参数集中，然后再返回否则就会创建新的请求参数集对象
     * @return 请求参数集
     */
    @SuppressWarnings("unchecked")
    public static RequestParams parseRequestParams(Context context, Class<? extends Request> requestClass, RequestParams requestParams){
        if(requestParams == null){
            requestParams = new RequestParams();
        }

        String requestParamName;
        String requestParamValue;
        Object requestParamValueObject = null;
        for(Field field : GeneralUtils.getFields(requestClass, true, true, true)){
            // 如果当前字段不是请求参数，或者不是静态的就跳过处理下一个字段
            if(!field.isAnnotationPresent(Param.class) || !Modifier.isStatic(field.getModifiers())){
                continue;
            }

            // 解析请求参数名称
            requestParamName = parseParamAnnotation(context, field);
            if(requestParamName == null){
                requestParamName = field.getName();
            }

            // 如果当前字段上有Value注解，就直接使用Value注解的值作为请求参数值
            if(field.isAnnotationPresent(Value.class)){
                String value = parseValueAnnotation(context, field);
                if(value != null){
                    requestParams.put(requestParamName, value);
                    continue;
                }
            }

            try {
                field.setAccessible(true);
                requestParamValueObject = field.get(null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 如果当前字段为null，就直接跳过处理下一个字段
            if(requestParamValueObject == null){
                continue;
            }

            // 如果当前字段是Map，就取出其中的每一项都添加到请求参数集中
            if(Map.class.isAssignableFrom(field.getType())){
                for(java.util.Map.Entry<Object, Object> entry : ((Map<Object, Object>) requestParamValueObject).entrySet()){
                    if(entry.getKey() != null && entry.getValue() != null){
                        String key =  entry.getKey().toString();
                        String value =  entry.getValue().toString();
                        if(GeneralUtils.isNotEmpty(key, value)){
                            requestParams.put(key, value);
                        }
                    }
                }
                continue;
            }

            // 如果当前字段是File，就将其作为一个文件添加到请求参水集中
            if(File.class.isAssignableFrom(field.getType())){
                try {
                    requestParams.put(requestParamName, (File) requestParamValueObject);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // 如果当前字段是ArrayList，就将其作为一个ArrayList添加到请求参数集中
            if(ArrayList.class.isAssignableFrom(field.getType())){
                requestParams.put(requestParamName, (ArrayList<String>) requestParamValueObject);
                continue;
            }

            // 如果当前字段是boolean
            if(Boolean.class.isAssignableFrom(field.getType())){
                if((Boolean) requestParamValueObject){
                    requestParamValue = parseTrueAnnotation(context, field);
                    if(requestParamValue == null){
                        requestParamValue = DEFAULT_VALUE_TRUE;
                    }
                }else{
                    requestParamValue = parseFalseAnnotation(context, field);
                    if(requestParamValue == null){
                        requestParamValue = DEFAULT_VALUE_FALSE;
                    }
                }
                requestParams.put(requestParamName, requestParamValue);
                continue;
            }

            // 如果当前字段是枚举
            if(Enum.class.isAssignableFrom(field.getType())){
                Enum<?> enumObject = (Enum<?>) requestParamValueObject;
                requestParamValue = parseValueAnnotationFromEnum(context, enumObject);
                if(requestParamValue == null){
                    requestParamValue = enumObject.name();
                }
                requestParams.put(requestParamName, requestParamValue);
                continue;
            }

            // 如果以上几种情况都不是就直接转为字符串添加到请求参数集中
            requestParamValue = requestParamValueObject.toString();
            if(GeneralUtils.isNotEmpty(requestParamValue)){
                requestParams.put(requestParamName, requestParamValue);
            }
        }

        return requestParams;
    }
    
    /**
     * 解析请求参数
     * @param context 上下文
     * @param requestClass 请求对象的class，一定要注意，从class上解析请求参数的时候只解析静态的字段
     * @return 请求参数
     */
    public static RequestParams parseRequestParams(Context context, Class<? extends Request> requestClass){
        return parseRequestParams(context, requestClass, null);
    }

    /**
     * 解析请求参数
     * @param context 上下文
     * @param request 请求对象
     * @param requestParams 请求参数集，如果不为null的话，会将解析到的请求参数放入此请求参数集中，然后再返回否则就会创建新的请求参数集对象
     * @return 请求参数
     */
    @SuppressWarnings("unchecked")
    public static RequestParams parseRequestParams(Context context, Request request, RequestParams requestParams){
        if(requestParams == null){
            requestParams = new RequestParams();
        }

        String requestParamName;
        String requestParamValue;
        Object requestParamValueObject = null;
        for(Field field : GeneralUtils.getFields(request.getClass(), true, true, true)){
            // 如果当前字段不是请求参数，就跳过处理下一个字段
            if(!field.isAnnotationPresent(Param.class)){
                continue;
            }

            // 解析请求参数名称
            requestParamName = parseParamAnnotation(context, field);
            if(requestParamName == null){
                requestParamName = field.getName();
            }

            // 如果当前字段上有Value注解，就直接使用Value注解的值作为请求参数值
            if(field.isAnnotationPresent(Value.class)){
                String value = parseValueAnnotation(context, field);
                if(value != null){
                    requestParams.put(requestParamName, value);
                    continue;
                }
            }

            try {
                field.setAccessible(true);
                requestParamValueObject = field.get(request);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 如果当前字段为null，就直接跳过处理下一个字段
            if(requestParamValueObject == null){
                continue;
            }

            // 如果当前字段是Map，就取出其中的每一项都添加到请求参数集中
            if(Map.class.isAssignableFrom(field.getType())){
                for(java.util.Map.Entry<Object, Object> entry : ((Map<Object, Object>) requestParamValueObject).entrySet()){
                    if(entry.getKey() != null && entry.getValue() != null){
                        String key =  entry.getKey().toString();
                        String value =  entry.getValue().toString();
                        if(GeneralUtils.isNotEmpty(key, value)){
                            requestParams.put(key, value);
                        }
                    }
                }
                continue;
            }

            // 如果当前字段是File，就将其作为一个文件添加到请求参水集中
            if(File.class.isAssignableFrom(field.getType())){
                try {
                    requestParams.put(requestParamName, (File) requestParamValueObject);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // 如果当前字段是ArrayList，就将其作为一个ArrayList添加到请求参数集中
            if(ArrayList.class.isAssignableFrom(field.getType())){
                requestParams.put(requestParamName, (ArrayList<String>) requestParamValueObject);
                continue;
            }

            // 如果当前字段是boolean
            if(Boolean.class.isAssignableFrom(field.getType())){
                if((Boolean) requestParamValueObject){
                    requestParamValue = parseTrueAnnotation(context, field);
                    if(requestParamValue == null){
                        requestParamValue = DEFAULT_VALUE_TRUE;
                    }
                }else{
                    requestParamValue = parseFalseAnnotation(context, field);
                    if(requestParamValue == null){
                        requestParamValue = DEFAULT_VALUE_FALSE;
                    }
                }
                requestParams.put(requestParamName, requestParamValue);
                continue;
            }

            // 如果当前字段是枚举
            if(Enum.class.isAssignableFrom(field.getType())){
                Enum<?> enumObject = (Enum<?>) requestParamValueObject;
                requestParamValue = parseValueAnnotationFromEnum(context, enumObject);
                if(requestParamValue == null){
                    requestParamValue = enumObject.name();
                }
                requestParams.put(requestParamName, requestParamValue);
                continue;
            }

            // 如果以上几种情况都不是就直接转为字符串添加到请求参数集中
            requestParamValue = requestParamValueObject.toString();
            if(GeneralUtils.isNotEmpty(requestParamValue)){
                requestParams.put(requestParamName, requestParamValue);
            }
        }

        return requestParams;
    }
    
    /**
     * 解析请求参数
     * @param context 上下文
     * @param request 请求对象
     * @return 请求参数
     */
    public static RequestParams parseRequestParams(Context context, Request request){
        return parseRequestParams(context, request, null);
    }

    /**
     * 解析请求头
     * @param request 请求对象
     */
    @SuppressWarnings("unchecked")
    public static org.apache.http.Header[] parseRequestHeaders(Request request){
        List<org.apache.http.Header> finalHeaders = null;
        for(Field field : GeneralUtils.getFields(request.getClass(), true, true, true)){
            field.setAccessible(true);
            if(field.getAnnotation(Header.class) != null){	//如果当前字段被标记为需要序列化
               try {
					Object value = field.get(request);
					if(value != null){
						if(org.apache.http.Header.class.isAssignableFrom(field.getType())){	//如果是单个
							if(finalHeaders == null){
								finalHeaders = new LinkedList<org.apache.http.Header>();
							}
	                    	finalHeaders.add((org.apache.http.Header) value);
		                }else if(GeneralUtils.isArrayByType(field, org.apache.http.Header.class)){	//如果Header数组
	                        org.apache.http.Header[] headers = (org.apache.http.Header[]) value;
	                        for(org.apache.http.Header header : headers){
	                            if(header != null){
	                            	if(finalHeaders == null){
	                            		finalHeaders = new LinkedList<org.apache.http.Header>();
	                            	}
	                            	finalHeaders.add(header);
	                            }
	                        }
		                }else if(GeneralUtils.isCollectionByType(field, Collection.class, org.apache.http.Header.class)){	//如果是Header集合
		                	if(finalHeaders == null){
		                		finalHeaders = new LinkedList<org.apache.http.Header>();
		                	}
	                        finalHeaders.addAll((Collection<org.apache.http.Header>) value);
		                }
					}
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				}
            }
        }

        if(finalHeaders != null && finalHeaders.size() > 0){
            org.apache.http.Header[] heades = new org.apache.http.Header[finalHeaders.size()];
            finalHeaders.toArray(heades);
            return heades;
        }else{
            return null;
        }
    }

    /**
     * 解析在生成缓存ID时需要忽略的参数
     * @param context 上下文
     * @param requestClass 请求对象的class
     */
    public static List<String> parseCacheIgnoreParams(Context context, Class<? extends Request> requestClass){
    	List<String> finalHeaders = null;
        for(Field field : GeneralUtils.getFields(requestClass, true, true, true)){
            field.setAccessible(true);
            if(field.getAnnotation(CacheIgnore.class) != null){	//如果当前字段被标记为在计算缓存ID的时候忽略
            	if(finalHeaders == null){
            		finalHeaders = new LinkedList<String>();
            	}
                String requestParamName = parseParamAnnotation(context, field);
                if(requestParamName == null){
                    requestParamName = field.getName();
                }
            	finalHeaders.add(requestParamName);
            }
        }
       return finalHeaders;
    }
    
    /**
     * 解析缓存ID
     * @param context 上下文
     * @param requestClass 请求对象的class
     */
    public static String parseCacheId(Context context, Class<? extends Request> requestClass){
       CacheConfig cacheConfig = parseResponseCacheAnnotation(context, requestClass);
       if(cacheConfig != null){
    	   return GeneralUtils.createCacheId(cacheConfig, parseBaseUrl(context, requestClass), parseRequestParams(context, requestClass), parseCacheIgnoreParams(context, requestClass));
       }else{
    	   return null;
       }
    }
    
    /**
     * 解析缓存ID
     * @param context 上下文
     * @param request 请求对象
     */
    public static String parseCacheId(Context context, Request request){
    	Class<? extends Request> requestClass = request.getClass();
    	CacheConfig cacheConfig = parseResponseCacheAnnotation(context, requestClass);
    	if(cacheConfig != null){
    		return GeneralUtils.createCacheId(cacheConfig, parseBaseUrl(context, requestClass), parseRequestParams(context, request), parseCacheIgnoreParams(context, requestClass));
    	}else{
    		return null;
    	}
    }


    /**
     * 解析False注解
     * @param context 上下文
     * @param field 待解析的字段
     */
	public static String parseFalseAnnotation(Context context, Field field){
    	False annotation = field.getAnnotation(False.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }
	
    /**
     * 解析主机地址注解
     * @param context 上下文
     * @param requestClass 请求对象
     */
    public static String parseHostAnnotation(Context context, Class<? extends Request> requestClass){
        Host annotation = requestClass.getAnnotation(Host.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析请求名称注解
     * @param context 上下文
     * @param requestClass 请求对象
     */
    public static String parseNameAnnotation(Context context, Class<? extends Request> requestClass){
        Name annotation = requestClass.getAnnotation(Name.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析请求参数名注解
     * @param context 上下文
     * @param field 字段
     */
    public static String parseParamAnnotation(Context context, Field field){
        Param annotation = field.getAnnotation(Param.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析路径注解
     * @param context 上下文
     * @param requestClass 请求对象
     */
    public static String parsePathAnnotation(Context context, Class<? extends Request> requestClass){
        Path annotation = requestClass.getAnnotation(Path.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析响应体注解的值
     * @param context 上下文
     * @param responseClass 响应对象的class
     */
    public static String parseResponseBodyAnnotation(Context context, Class<?> responseClass){
    	ResponseBody annotation = responseClass.getAnnotation(ResponseBody.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析响应缓存配置信息
     * @param context 上下文
     * @param requestClass 请求对象的class
     */
    public static CacheConfig parseResponseCacheAnnotation(Context context, Class<? extends Request> requestClass){
        me.xiaopan.android.gohttp.annotation.CacheConfig annotation = requestClass.getAnnotation(me.xiaopan.android.gohttp.annotation.CacheConfig.class);
        if(annotation == null){
            return null;
        }
        
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setRefreshCache(annotation.isRefreshCache());
        cacheConfig.setPeriodOfValidity(annotation.periodOfValidity());
        cacheConfig.setRefreshCallback(annotation.isRefreshCallback());
        if(GeneralUtils.isNotEmpty(annotation.cacheDirectory())){
            cacheConfig.setCacheDirectory(annotation.cacheDirectory());
        }else if(context != null && annotation.cacheDirectoryResId() > 0){
            cacheConfig.setCacheDirectory(context.getString(annotation.cacheDirectoryResId()));
        }
        return cacheConfig;
    }

    /**
     * 解析True注解
     * @param context 上下文
     * @param field 待解析的字段
     */
    public static String parseTrueAnnotation(Context context, Field field){
        True annotation = field.getAnnotation(True.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析URL注解
     * @param context 上下文
     * @param requestClass 请求对象
     */
    public static String parseURLAnnotation(Context context, Class<? extends Request> requestClass){
        URL annotation = requestClass.getAnnotation(URL.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析请求参数值注解
     * @param context 上下文
     * @param field 字段
     */
    public static String parseValueAnnotation(Context context, Field field){
        Value annotation = field.getAnnotation(Value.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }

    /**
     * 解析枚举上的Value注解
     * @param context 上下文
     * @param enumObject 枚举对象
     */
    public static String parseValueAnnotationFromEnum(Context context, Enum<?> enumObject){
        Value annotation = GeneralUtils.getAnnotationFromEnum(enumObject, Value.class);
        if(annotation == null){
            return null;
        }
        if(GeneralUtils.isNotEmpty(annotation.value())){
            return annotation.value();
        }else if(context != null && annotation.resId() > 0){
            return context.getString(annotation.resId());
        }else{
            return null;
        }
    }
}
