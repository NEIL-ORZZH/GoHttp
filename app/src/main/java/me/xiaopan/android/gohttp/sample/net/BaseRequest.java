/*
 * Copyright 2013 Peng fei Pan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xiaopan.android.gohttp.sample.net;

import me.xiaopan.android.gohttp.requestobject.Request;
import me.xiaopan.android.gohttp.requestobject.Method;
import me.xiaopan.android.gohttp.MethodType;

/**
 * 基本请求，可以将一些每个请求都必须有的参数定义在此
 */
@Method(MethodType.GET)
public class BaseRequest implements Request {

}