package com.joe.app.outbound.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.joe.app.baseutil.ui.BaseActivity;
import com.joe.app.baseutil.util.JSONUtils;
import com.joe.app.baseutil.util.MUtils;
import com.joe.app.outbound.R;
import com.joe.app.outbound.data.Api;
import com.joe.app.outbound.data.listener.OnNetRequest;
import com.joe.app.outbound.data.model.EmployeeBean;
import com.joe.app.outbound.data.model.RetailCustomer;
import com.joe.app.outbound.data.model.RetailOrderBean;
import com.joe.app.outbound.ui.widget.ClearEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 选择零售单客户
 *
 * @author zongdongdong
 */
public class SelectRetailCustomerActivity extends BaseActivity {
    @Bind(R.id.txtvActionbarTitle)
    TextView txtvActionbarTitle;
    @Bind(R.id.txtvRight)
    TextView txtvRight;
    @Bind(R.id.etSearch)
    ClearEditText etSearch;
    @Bind(R.id.listView)
    ListView listView;

    private EmployeeBean mEmployeeBean;
    private RetailCustomerAdapter adapter;
    private int requestCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_retail_order);
        ButterKnife.bind(this);
        setViews();
        setClickListeners();
    }

    private void setViews() {
        mEmployeeBean = (EmployeeBean) getIntent().getExtras().getSerializable(EmployeeBean.class.getSimpleName());
        txtvActionbarTitle.setText("新增零售单");
        txtvRight.setText(mEmployeeBean.name);
        adapter = new RetailCustomerAdapter();
        listView.setAdapter(adapter);
    }

    private void setClickListeners() {
        etSearch.setOnInputChange(new ClearEditText.OnInputChange() {
            @Override
            public void onAfterTextChange() {
                String keyWord = etSearch.getText().toString();
                if (TextUtils.isEmpty(keyWord)) {
                    adapter.refresh(null);
                } else {
                    searchRetailCustomer(keyWord);
                }
            }
        });

        /**
         * 选择客户，点击后call api生成订单
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RetailCustomer.Result result = (RetailCustomer.Result)adapter.getItem(position);
                addRetailOrder(result.id);
            }
        });
    }

    /**
     * 搜索客户名单
     *
     * @param key
     */
    private synchronized void searchRetailCustomer(String key) {
        requestCount++;
        Api api = new Api(this, new OnNetRequest(this) {
            @Override
            public void onSuccess(String msg) {
                RetailCustomer result = JSONUtils.fromJson(msg, RetailCustomer.class);
                adapter.refresh(result.result);
            }

            @Override
            public void onFail() {

            }
        });
        api.getCompany(key, requestCount);
    }

    /**
     * 新增零售单
     * @param companyId
     */
    private void addRetailOrder(String companyId){
        Api api = new Api(this, new OnNetRequest(this, true, "请稍等...") {
            @Override
            public void onSuccess(String msg) {
                try{
                    JSONObject jsonObject = new JSONObject(msg);
                    RetailOrderBean.Data data = JSONUtils.fromJson(jsonObject.optString("result"), RetailOrderBean.Data.class);
                    Intent intent = new Intent(SelectRetailCustomerActivity.this, SaleSendDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EmployeeBean.class.getSimpleName(), mEmployeeBean);
                    bundle.putSerializable(RetailOrderBean.class.getSimpleName(), data);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {

            }
        });
        api.addRetailOrder(mEmployeeBean.id, companyId);
    }

    public class RetailCustomerAdapter extends BaseAdapter {
        List<RetailCustomer.Result> listData = new ArrayList<>();

        public synchronized void refresh(List<RetailCustomer.Result> ls) {
            if (ls == null) {
                ls = new ArrayList<>();
            }
            listData = ls;
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_retail_customer, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            RetailCustomer.Result data = listData.get(position);
            viewHolder.txtvCompanyName.setText(data.name);
            return convertView;
        }
    }
    static class ViewHolder {
        @Bind(R.id.txtvCompanyName)
        TextView txtvCompanyName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @OnClick(R.id.txtvLeft)
    public void onBackClickListener() {
        MUtils.hideSoftInputOfView(this, etSearch);
        finish();
    }

    @Override
    protected void onStop() {
        MUtils.hideSoftInputOfView(this, etSearch);
        super.onStop();
    }
}
