package com.joe.app.outbound.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.joe.app.outbound.AppConstant;
import com.joe.app.outbound.MyApplication;

/**
 * Created by Joe on 2016/6/9.
 * Email-joe_zong@163.com
 */
public class SharedPreference {
    public static final SharedPreferences mSharedPreference = MyApplication.getInstance().getSharedPreferences("OutboundPreferences", Context.MODE_PRIVATE);

    public static final String EmplyeeId = ".employee_id";
    public static String getEmplyeeId(){
        return mSharedPreference.getString(EmplyeeId,"");
    }

    public static void setEmplyeeId(String emplyeeId){
        mSharedPreference.edit().putString(EmplyeeId,emplyeeId).apply();
    }

    public static final String Host = ".host";

    public static String getHost() {
        String host = mSharedPreference.getString(Host, AppConstant.Host);
        return TextUtils.isEmpty(host)?AppConstant.Host:host;
    }

    public static void setHost(String host){
        mSharedPreference.edit().putString(Host,host).apply();
    }


}
