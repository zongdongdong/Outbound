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
import java.util.HashMap;
import java.util.Map;

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

    /**
     * 获取员工信息
     */
    public final static String GET_EMPLOYEE = "employee/list";
    public void getEmployeeInfo(){
        mClient.get(GET_EMPLOYEE,null,callback);
    }

    /**
     * 根据销售发货单据id获取该订单的所有发货码单的列表
     */
    public final static String GET_PACKAGE_LIST = "salesend/listpack";
    public void getPackageList(String order_id){
        Map<String,String> params = new HashMap<>();
        params.put("order_id",order_id);
        mClient.get(GET_PACKAGE_LIST, params, callback);
    }

    /**
     * 扫码出库操作
     * "order_id" : 1,                 // 单据id
       "employee_id" : 1,              // 员工id
       "barcode" : "6912111120373",    // 条码 6913111538101 / 6913111538132 / 6913111538154
        "bale" : '1#'                   // 包号
     */
    public final static String POST_ADD_PACKAGE = "salesend/addpack";
    public void addPackage(String order_id, String employee_id, String barcode, String bale){
        Map<String,String> params = new HashMap<>();
        params.put("order_id",order_id);
        params.put("employee_id",employee_id);
        params.put("barcode",barcode);
        params.put("bale",bale);
        mClient.post(POST_ADD_PACKAGE,params,callback);
    }

    /**
     * 删除出库条码操作
     */
    public final static String POST_DELETE_PACKAGE = "salesend/delpack";
    public void deletePackage(String id){
        Map<String,String> params = new HashMap<>();
        params.put("id",id);
        mClient.post(POST_DELETE_PACKAGE,params,callback);
    }
}
