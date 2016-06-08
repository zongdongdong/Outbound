package com.joe.app.outbound.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.joe.app.baseutil.ui.BaseActivity;
import com.joe.app.outbound.R;
import com.joe.app.outbound.data.model.EmployeeBean;
import com.joe.app.outbound.data.model.SaleSendOrderBean;

public class SaleSendDetailActivity extends BaseActivity {

    private EmployeeBean mEmployee;
    private SaleSendOrderBean saleSendOrder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_send_detail);
        if(getIntent()!=null){
            mEmployee = (EmployeeBean)getIntent().getSerializableExtra(EmployeeBean.class.getSimpleName());
            saleSendOrder = (SaleSendOrderBean)getIntent().getSerializableExtra(SaleSendOrderBean.class.getSimpleName());
        }
    }
}
