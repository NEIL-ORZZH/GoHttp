package me.xiaopan.android.gohttp.sample.net.request;

import me.xiaopan.android.gohttp.annotation.CacheConfig;
import me.xiaopan.android.gohttp.annotation.Param;
import me.xiaopan.android.gohttp.annotation.URL;
import me.xiaopan.android.gohttp.sample.net.BaseRequest;

/**
 * Created by xiaopan on 13-11-27.
 */
@URL("http://www.qiushibaike.com/article/52638010")
@CacheConfig(periodOfValidity = 1000 * 60 * 60 * 24, isRefreshCache = true)
public class QiuBaiRequest extends BaseRequest{
    @Param
    private String list = "8hr";

    @Param
    private String s = "4618412";
}
