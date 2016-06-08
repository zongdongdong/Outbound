package com.joe.app.outbound.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jingchen.pulltorefresh.PullToRefreshLayout;
import com.joe.app.baseutil.ui.BaseActivity;
import com.joe.app.baseutil.util.JSONUtils;
import com.joe.app.outbound.R;
import com.joe.app.outbound.data.Api;
import com.joe.app.outbound.data.listener.OnNetRequest;
import com.joe.app.outbound.data.model.SaleSendOrderBean;
import com.joe.app.outbound.data.model.SaleSendOrderResponse;
import com.joe.app.outbound.ui.adapter.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @Bind(R.id.txtvActionbarTitle)
    TextView txtvActionbarTitle;
    @Bind(R.id.spinner)
    Spinner spinner;
    @Bind(R.id.pullToRefreshLayout)
    PullToRefreshLayout pullToRefreshLayout;

    ListView pullListView;
    private SalesendAdapter adapter;

    private SpinnerAdapter spinnerAdapter;

//    private DataProvider mDataProvider;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        mDataProvider = DataProvider.getInstance();
        setViews();
        setClickListeners();
        getSaleSendList(true);
    }

    private void setViews() {
        txtvActionbarTitle.setText("出库扫码");
        pullToRefreshLayout.setPullUpEnable(false);
        pullListView = (ListView) pullToRefreshLayout.getPullableView();
        adapter = new SalesendAdapter();
        pullListView.setAdapter(adapter);
        spinnerAdapter = new SpinnerAdapter();
        spinner.setAdapter(spinnerAdapter);
    }

    private void setClickListeners(){
        pullToRefreshLayout.setOnPullListener(new PullToRefreshLayout.OnPullListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                getSaleSendList(false);
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

            }
        });
    }

    //获取员工信息
    public void getEmployeeInfo(){
        Api api = new Api(this, new OnNetRequest(this) {
            @Override
            public void onSuccess(String msg) {

            }

            @Override
            public void onFail() {

            }
        });
        api.getEmployeeInfo();
    }

    //获取销售发货单
    public void getSaleSendList(boolean isShowLoading){
        Api api = new Api(this, new OnNetRequest(this,isShowLoading,"正在加载...") {
            @Override
            public void onSuccess(String msg) {
                SaleSendOrderResponse response = JSONUtils.fromJson(msg,SaleSendOrderResponse.class);
                if(response!=null && response.result!=null){
                    if(response.result.size()>0){
                        adapter.refresh(response.result);
                    }
                    pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
            }

            @Override
            public void onFail() {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            }
        });
        api.getSaleSendOrderInfoList();
    }



    class SalesendAdapter extends BaseAdapter {
        List<SaleSendOrderBean> listData = new ArrayList<>();

        public void refresh(List<SaleSendOrderBean> ls) {
            if (ls == null) {
                listData = new ArrayList<>();
            } else {
                listData = ls;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_sale_send, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }
            SaleSendOrderBean bean = listData.get(position);
            viewHolder.txtvCustomcode.setText(bean.customcode);
            viewHolder.txtvCustomerName.setText(bean.customer_name);
            viewHolder.txtvMaterial.setText(bean.material);
            if(TextUtils.isEmpty(bean.craft)){
                viewHolder.txtvColor.setText(bean.color);
            }else{
                viewHolder.txtvColor.setText(bean.color+"["+bean.craft+"]");
            }
            viewHolder.txtvBilldate.setText(bean.billdate);
            viewHolder.txtvPlanQuantity.setText(bean.plan_quantity);
            return convertView;
        }

    }
    static class ViewHolder {
        @Bind(R.id.txtv_customcode)
        TextView txtvCustomcode;
        @Bind(R.id.txtv_customer_name)
        TextView txtvCustomerName;
        @Bind(R.id.txtv_material)
        TextView txtvMaterial;
        @Bind(R.id.txtv_color)
        TextView txtvColor;
        @Bind(R.id.txtv_plan_quantity)
        TextView txtvPlanQuantity;
        @Bind(R.id.txtv_billdate)
        TextView txtvBilldate;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
