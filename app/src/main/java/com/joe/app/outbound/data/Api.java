package com.joe.app.outbound.data;

import android.content.Context;
import android.util.Log;

import com.joe.app.baseutil.api.Client;
import com.joe.app.baseutil.api.OnRequestCallback;
import com.joe.app.baseutil.util.UIHelper;
import com.joe.app.outbound.AppConstant;
import com.joe.app.outbound.data.listener.OnNetRequest;
import com.squareup.okhttp.Request;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Joe on 2016/6/7.
 * Email-joe_zong@163.com
 */
public class Api {
    private Context mContext;
    private OnRequestCallback callback;
    private Client mClient;
    public Api(Context aContext, final OnNetRequest listener){
        this.mContext = aContext;
        this.mClient = Client.getInstance(AppConstant.Host);
        callback = new OnRequestCallback(mContext,listener.isShowLoading(),listener.getLoadingText()) {
            @Override
            public void onResponse(String response) {
                super.onResponse(response);
                Log.i("Response",response);
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.optBoolean("status",false)){
                        listener.onSuccess(response);
                    }else{
                        UIHelper.showLongToast(mContext,jsonObject.optString("result"));
                        listener.onFail();
                    }
                }catch (Exception e){
                    UIHelper.showLongToast(mContext,"Json解析错误");
                    listener.onFail();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                super.onError(request, e);
                if(e instanceof IOException){
                    UIHelper.showLongToast(mContext,"请求出错，检查网络是否正常");
                    listener.onFail();
                }else{
                    UIHelper.showLongToast(mContext,"未知错误");
                    listener.onFail();
                }
            }
        };
    }

    /**
     * 获取销售发货单
     */
    public final static String GET_SALESEND = "salesend/list";
    public void getSaleSendOrderInfoList(){
        mClient.get(GET_SALESEND, null, callback);
    }

}
