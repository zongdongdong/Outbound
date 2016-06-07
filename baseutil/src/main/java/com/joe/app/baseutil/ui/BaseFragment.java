package com.joe.app.baseutil.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Joe on 2016/6/4.
 */
public class BaseFragment extends Fragment {
    public BaseFragmentActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        this.mActivity = (BaseFragmentActivity)activity;
//    }

    public BaseFragmentActivity getParentActivity(){
        return mActivity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (BaseFragmentActivity)context;
    }

}
