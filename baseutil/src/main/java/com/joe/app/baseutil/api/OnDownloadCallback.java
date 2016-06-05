package com.joe.app.baseutil.api;

import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;

/**
 * Created by Joe on 2016/6/5.
 * Email-joe_zong@163.com
 */
public class OnDownloadCallback extends FileCallBack {
    public OnDownloadCallback(String filePath, String fileName){
        super(filePath, fileName);
    }

    @Override
    public void onResponse(File response) {

    }

    @Override
    public void inProgress(float progress) {

    }

    @Override
    public void onError(Request request, Exception e) {

    }
}
