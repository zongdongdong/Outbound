package com.joe.app.outbound.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jingchen.pulltorefresh.PullToRefreshLayout;
import com.joe.app.baseutil.ui.BaseActivity;
import com.joe.app.baseutil.util.JSONUtils;
import com.joe.app.baseutil.util.UIHelper;
import com.joe.app.outbound.R;
import com.joe.app.outbound.data.Api;
import com.joe.app.outbound.data.SharedPreference;
import com.joe.app.outbound.data.event.HostChangeEvent;
import com.joe.app.outbound.data.listener.OnNetRequest;
import com.joe.app.outbound.data.model.EmployeeBean;
import com.joe.app.outbound.data.model.EmployeeResponseBean;
import com.joe.app.outbound.data.model.RetailOrderBean;
import com.joe.app.outbound.data.model.SaleSendOrderBean;
import com.joe.app.outbound.data.model.SaleSendOrderResponse;
import com.joe.app.outbound.ui.adapter.SpinnerAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @Bind(R.id.txtvActionbarTitle)
    TextView txtvActionbarTitle;
    @Bind(R.id.spinner)
    Spinner spinner;
    @Bind(R.id.pullToRefreshLayout)
    PullToRefreshLayout pullToRefreshLayout;

    ListView pullListView;
    @Bind(R.id.btnSelectOrder)
    Button btnSelectOrder;
    @Bind(R.id.btnAddNewOrder)
    Button btnAddNewOrder;
    private RetailOrderAdapter adapter;

    private SpinnerAdapter spinnerAdapter;
    private EmployeeBean currentEmployee;

    private List<RetailOrderBean.Data> retailOrderList;
    private EasterEggCounter mEasterEggCounter;

    private String currentEmployeeId;

//    private DataProvider mDataProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        mDataProvider = DataProvider.getInstance();
        setViews();
        setClickListeners();
        getEmployeeInfo();
    }

    private void setViews() {
        txtvActionbarTitle.setText("零售扫码");
        btnAddNewOrder.setEnabled(false);
        btnSelectOrder.setEnabled(false);


        pullToRefreshLayout.setPullUpEnable(false);
        pullListView = (ListView) pullToRefreshLayout.getPullableView();
        adapter = new RetailOrderAdapter();
        pullListView.setAdapter(adapter);
        spinnerAdapter = new SpinnerAdapter();
        spinner.setAdapter(spinnerAdapter);
        mEasterEggCounter = new EasterEggCounter();
    }

    private void setClickListeners() {
        pullToRefreshLayout.setOnPullListener(new PullToRefreshLayout.OnPullListener() {
            @Override
            public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
                getRetailOrderListWithEmployeeId(currentEmployeeId, false);
            }

            @Override
            public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentEmployee = (EmployeeBean) spinnerAdapter.getItem(position);
                SharedPreference.setEmplyeeId(currentEmployee.id);
                currentEmployeeId = currentEmployee.id;
                if (currentEmployee.id.equals("-1")) {
                    btnAddNewOrder.setEnabled(false);
                    btnSelectOrder.setEnabled(false);
                } else {
                    btnAddNewOrder.setEnabled(true);
                    btnSelectOrder.setEnabled(true);
                }
                getRetailOrderListWithEmployeeId(currentEmployeeId, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        pullListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentEmployee == null || currentEmployee.id.equals("-1")) {
                    UIHelper.showShortToast(MainActivity.this, "请选择员工");
                } else {
                    RetailOrderBean.Data data = (RetailOrderBean.Data) adapter.getItem(position);
                    Intent intent = new Intent(MainActivity.this, SaleSendDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(EmployeeBean.class.getSimpleName(), currentEmployee);
                    bundle.putSerializable(RetailOrderBean.class.getSimpleName(), data);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        /**
         * 选择客户新增零售单
         */
        btnSelectOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectRetailCustomerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(EmployeeBean.class.getSimpleName(), currentEmployee);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        /**
         * 快捷新增，直接call api获取零售客户信息
         */
        btnAddNewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Api api = new Api(MainActivity.this, new OnNetRequest(MainActivity.this, true, "正在新增...") {
                    @Override
                    public void onSuccess(String msg) {
                        try {
                            JSONObject jsonObject = new JSONObject(msg);
                            RetailOrderBean.Data data = JSONUtils.fromJson(jsonObject.optString("result"), RetailOrderBean.Data.class);
                            Intent intent = new Intent(MainActivity.this, SaleSendDetailActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(EmployeeBean.class.getSimpleName(), currentEmployee);
                            bundle.putSerializable(RetailOrderBean.class.getSimpleName(), data);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail() {

                    }
                });
                api.addQuickRetailOrder(currentEmployeeId);
            }
        });
    }

    //获取员工信息
    public void getEmployeeInfo() {
        Api api = new Api(this, new OnNetRequest(this, true, "正在加载...") {
            @Override
            public void onSuccess(String msg) {
                EmployeeResponseBean employeeResponseBean = JSONUtils.fromJson(msg, EmployeeResponseBean.class);
                if (employeeResponseBean != null && employeeResponseBean.result != null) {
                    spinnerAdapter.refresh(employeeResponseBean.result);
                    String employeeId = SharedPreference.getEmplyeeId();
                    if (TextUtils.isEmpty(employeeId) || employeeId.equals("-1")) {
                        return;
                    }
                    for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                        EmployeeBean bean = (EmployeeBean) spinnerAdapter.getItem(i);
                        if (bean.id.equals(employeeId)) {
                            spinner.setSelection(i);
                            btnAddNewOrder.setEnabled(true);
                            btnSelectOrder.setEnabled(true);
                            currentEmployeeId = bean.id;
                            getRetailOrderListWithEmployeeId(currentEmployeeId, false);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFail() {
                currentEmployee = null;
            }
        });
        api.getEmployeeInfo();
    }

    /**
     * 获取零售单列表
     *
     * @param employeeId
     */
    private void getRetailOrderListWithEmployeeId(String employeeId, boolean isShowing) {
        if (TextUtils.isEmpty(employeeId) || "-1".equals(employeeId)) {
            adapter.refresh(null);
            return;
        }
        Api api = new Api(this, new OnNetRequest(this, isShowing, "正在加载...") {
            @Override
            public void onSuccess(String msg) {
                RetailOrderBean retailOrderBean = JSONUtils.fromJson(msg, RetailOrderBean.class);
                if (retailOrderBean != null && retailOrderBean.result != null) {
                    retailOrderList = retailOrderBean.result;
                    adapter.refresh(retailOrderList);
                    pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                }
            }

            @Override
            public void onFail() {
                adapter.refresh(retailOrderList);
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            }
        });
        api.getRetailOrderList(employeeId);
    }

    public void delRetail(String id, final int position) {
        Api api = new Api(this, new OnNetRequest(this, true, "正在删除...") {
            @Override
            public void onSuccess(String msg) {
                UIHelper.showShortToast(MainActivity.this, "删除成功");
//                RetailOrderBean.Data retailOrderBean = JSONUtils.fromJson(msg, RetailOrderBean.Data.class);
//                if (retailOrderBean != null && retailOrderBean.result != null) {
//                    retailOrderList = retailOrderBean.result;
//                    adapter.refresh(retailOrderList);
//                }
                adapter.del(position);
            }

            @Override
            public void onFail() {

            }
        });
        api.delRetail(id);
    }

    @OnClick(R.id.txtvActionbarTitle)
    public void onTitleClickListener() {
        mEasterEggCounter.tapped();
    }

//    @Subscribe
//    public void onChangeHostEvent(HostChangeEvent event) {
//        UIHelper.post(new Runnable() {
//            @Override
//            public void run() {
//                getRetailOrderListWithEmployeeId(currentEmployeeId, false);
//            }
//        });
//    }

    @Override
    protected void onResume() {
        super.onResume();
        getRetailOrderListWithEmployeeId(currentEmployeeId, false);
    }

    class RetailOrderAdapter extends BaseAdapter {
        private Filter filter;

        List<RetailOrderBean.Data> listData = new ArrayList<>();

        public void refresh(List<RetailOrderBean.Data> ls) {
            if (ls == null) {
                listData = new ArrayList<>();
            } else {
                listData = ls;
            }
            notifyDataSetChanged();
        }

        public void del(int position) {
            if (position < listData.size()) {
                listData.remove(position);
                notifyDataSetChanged();
            }
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_retail_order, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final RetailOrderBean.Data data = listData.get(position);
            viewHolder.txtvCode.setText(data.code);
            viewHolder.txtvBilldate.setText(data.billdate);
            viewHolder.txtvCustomerName.setText(data.customer_name);
            viewHolder.txtvQuantityHtml.setText(data.quantity_string);
            viewHolder.txtvDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String id = data.id;
                    String name = data.customer_name;
                    String barCode = data.code;
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("是否确认删除此零售单\n" + name + "\n" + barCode + "?");
                    builder.setTitle("提示");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            delRetail(id, position);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        @Bind(R.id.txtvDelete)
        TextView txtvDel;
        @Bind(R.id.txtvCode)
        TextView txtvCode;
        @Bind(R.id.txtvCustomerName)
        TextView txtvCustomerName;
        @Bind(R.id.txtvQuantityHtml)
        TextView txtvQuantityHtml;
        @Bind(R.id.txtvBilldate)
        TextView txtvBilldate;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

//    class SaleSendOrderFilter extends Filter {
//        @Override
//        protected FilterResults performFiltering(CharSequence prefix) {
//            FilterResults filterResults = new FilterResults();
//            if (prefix == null || prefix.length() == 0 || saleSendOrderBeanList == null || saleSendOrderBeanList.size() == 0) {
//                ArrayList<SaleSendOrderBean> l = new ArrayList<>(saleSendOrderBeanList);
//                filterResults.values = l;
//                filterResults.count = l.size();
//            } else {
//                String prefixString = prefix.toString().toLowerCase();
//                final List<SaleSendOrderBean> list = new ArrayList<>(saleSendOrderBeanList);
//                final List<SaleSendOrderBean> newList = new ArrayList<>();
//                for (int i = 0; i < list.size(); i++) {
//                    String id = list.get(i).customcode.toString().trim();
//                    if (id.contains(prefixString)) {
//                        newList.add(list.get(i));
//                    }
//                }
//                filterResults.values = newList;
//                filterResults.count = newList.size();
//            }
//            return filterResults;
//        }
//
//        @Override
//        protected void publishResults(CharSequence constraint, FilterResults results) {
//            List<SaleSendOrderBean> list = (List<SaleSendOrderBean>) results.values;
//            adapter.refresh(list);
////            if (results.count > 0) {
////                adapter.refresh(list);
////            }
//        }
//    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("确认退出吗?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MainActivity.this.finish();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private class EasterEggCounter {
        private static final int MAX_TAP_COUNT = 10;
        private static final long TIME_TO_DISMISS_MILLIS = 2500;

        private int mTapCount = 0;
        private Thread mDismissThread;

        /**
         * Method for counting number of taps
         */

        public synchronized void tapped() {
            if (mDismissThread == null) {
                mDismissThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(TIME_TO_DISMISS_MILLIS);
                            reset();
                        } catch (InterruptedException ie) {
                            reset();
                        }
                    }
                });

                mDismissThread.start();
            }

            mTapCount++;
            if (mTapCount == MAX_TAP_COUNT) {
                reset();

                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                MainActivity.this.startActivity(intent);
            }
        }

        /**
         * Method for resetting number of taps
         */
        private synchronized void reset() {
            mTapCount = 0;
            mDismissThread = null;
        }
    }

//    @Subscribe(priority = 1)
//    public void OnScanResultEvent(final ScanResultEvent event) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                MUtils.hideSoftInput(MainActivity.this);
//                etSearch.setText(event.getResult());
//            }
//        });
//    }
}
