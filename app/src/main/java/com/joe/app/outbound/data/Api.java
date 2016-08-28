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
import java.net.ConnectException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Joe on 2016/6/7.
 * Email-joe_zong@163.com
 */
public class Api {
    private Context mContext;
    private OnRequestCallback callback;
    private Client mClient;

    public Api(Context aContext, final OnNetRequest listener) {
        this.mContext = aContext;
        this.mClient = Client.getInstance(SharedPreference.getHost());
        callback = new OnRequestCallback(mContext, listener.isShowLoading(), listener.getLoadingText()) {
            @Override
            public void onResponse(String response) {
                super.onResponse(response);
                Log.i("Response", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.optBoolean("status", false)) {
                        listener.onSuccess(response);
                    } else {
                        String errorMessage = jsonObject.optString("result");
                        Log.e("Response", errorMessage);
                        UIHelper.showLongToast(mContext, errorMessage);
                        listener.onFail();
                    }
                } catch (Exception e) {
                    UIHelper.showLongToast(mContext, "Json解析错误");
                    listener.onFail();
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                super.onError(request, e);
                if (e instanceof TimeoutException) {
                    UIHelper.showLongToast(mContext, "请求超时");
                    listener.onFail();
                } else if (e instanceof ConnectException) {
                    UIHelper.showLongToast(mContext, "请求出错，检查网络是否正常");
                    listener.onFail();
                } else {
                    UIHelper.showLongToast(mContext, "未知错误");
                    listener.onFail();
                }
            }
        };
    }

    /**
     * 获取销售发货单
     */
    public final static String GET_SALESEND = "salesend/list";

    public void getSaleSendOrderInfoList() {
        mClient.get(GET_SALESEND, null, callback);
    }

    /**
     * 获取员工信息
     */
    public final static String GET_EMPLOYEE = "employee/list";

    public void getEmployeeInfo() {
        mClient.get(GET_EMPLOYEE, null, callback);
    }

    /**
     * 根据销售发货单据id获取该订单的所有发货码单的列表
     */
    public final static String GET_PACKAGE_LIST = "salesend/listpack";

    public void getPackageList(String order_id) {
        Map<String, String> params = new HashMap<>();
        params.put("order_id", order_id);
        mClient.get(GET_PACKAGE_LIST, params, callback);
    }



    //--------------------------------------------new device2--------------------------------------

    /**
     * 零售单列表接口
     */
    public void getRetailOrderList(String employeeId) {
        Map<String, String> params = new HashMap<>();
        params.put("employee_id", employeeId);
        mClient.get("retail/list", params, callback);
    }

    /**
     * 客户接口
     */
    public void getCompany(String keyword, int tag){
        mClient.cancelTag(tag-1);
        Map<String, String> params = new HashMap<>();
        params.put("query", keyword);
        mClient.get("company/list", params, tag, callback);
    }

    /**
     * 新增零售单接口
     */
    public void addRetailOrder(String employee_id, String company_id){
        Map<String, String> params = new HashMap<>();
        params.put("employee_id", employee_id);
        params.put("company_id", company_id);
        mClient.post("retail/addnormal", params, callback);
    }

    /**
     * 零售单码单列表接口
     */
    public void getRetailOrderPackList(String order_id){
        Map<String, String> params = new HashMap<>();
        params.put("order_id", order_id);
        mClient.get("retail/listpack", params, callback);
    }

    /**
     * 删除条码明细接口
     */
    public final static String POST_DELETE_PACKAGE = "retail/delpack";

    public void deletePackage(String id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        mClient.post(POST_DELETE_PACKAGE, params, callback);
    }

    /**
     * 扫码出库操作
     * order_id	零售单id
     * isvolume	整卷状态（1/0）
     * quantity	数量
     * barcode	条形码
     */
    public final static String POST_ADD_PACKAGE = "retail/addpack";

    public void addPackage(String order_id, String isvolume, String quantity, String barcode) {
        Map<String, String> params = new HashMap<>();
        params.put("order_id", order_id);
        params.put("quantity", quantity);
        params.put("barcode", barcode);
        params.put("isvolume", isvolume);
        mClient.post(POST_ADD_PACKAGE, params, callback);
    }

    /**
     * 审核零售单接口
     * @param order_id
     */
    public void submitRetailOrder(String order_id){
        Map<String, String> params = new HashMap<>();
        params.put("order_id", order_id);
        mClient.post("retail/audit", params, callback);
    }

    /**
     * 快捷新增零售单接口
     */
    public void addQuickRetailOrder(String employee_id){
        Map<String, String> params = new HashMap<>();
        params.put("employee_id", employee_id);
        mClient.post("retail/addquick", params, callback);
    }

    /**
     * 删除零售单接口
     */
    public void delRetail(String order_id){
        Map<String, String> params = new HashMap<>();
        params.put("order_id", order_id);
        mClient.post("retail/del", params, callback);
    }
}
