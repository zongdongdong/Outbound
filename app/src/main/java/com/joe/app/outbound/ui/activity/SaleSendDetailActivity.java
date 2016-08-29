package com.joe.app.outbound.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.joe.app.baseutil.ui.BaseActivity;
import com.joe.app.baseutil.util.JSONUtils;
import com.joe.app.baseutil.util.MUtils;
import com.joe.app.baseutil.util.UIHelper;
import com.joe.app.outbound.R;
import com.joe.app.outbound.data.Api;
import com.joe.app.outbound.data.event.ScanResultEvent;
import com.joe.app.outbound.data.listener.OnNetRequest;
import com.joe.app.outbound.data.model.EmployeeBean;
import com.joe.app.outbound.data.model.RetailOrderBean;
import com.joe.app.outbound.data.model.RetailOrderPackBean;
import com.joe.app.outbound.ui.dialog.InputPackageNumDialog;
import com.joe.app.outbound.ui.widget.ClearEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SaleSendDetailActivity extends BaseActivity {

    @Bind(R.id.txtvActionbarTitle)
    TextView txtvActionbarTitle;
    @Bind(R.id.txtvRight)
    TextView txtvRight;
    @Bind(R.id.txtvCustomerName)
    TextView txtvCustomerName;
    @Bind(R.id.txtvCount)
    TextView txtvCount;
    @Bind(R.id.etScanCode)
    ClearEditText etScanCode;
    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.txtvCheckImage)
    TextView txtvCheckImage;//整卷是否选中图片
    @Bind(R.id.txtvAllWeight)
    TextView txtvAllWeight;
    @Bind(R.id.txtvConfirm)
    TextView txtvConfirm;
    @Bind(R.id.btnSubmit)
    Button btnSubmit;
    private EmployeeBean mEmployee;
    private RetailOrderBean.Data mRetailOrder;
    private InputPackageNumDialog inputDialog;
    private PackageListAdapter adapter;

    private boolean isFullVolume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_send_detail);
        ButterKnife.bind(this);
        if (getIntent() != null) {
            mEmployee = (EmployeeBean) getIntent().getSerializableExtra(EmployeeBean.class.getSimpleName());
            mRetailOrder = (RetailOrderBean.Data) getIntent().getSerializableExtra(RetailOrderBean.class.getSimpleName());
        }
        setViews();
    }

    private void setViews() {
        adapter = new PackageListAdapter();
        listView.setAdapter(adapter);
//        listView.setEnabled(false);
        listView.setSelected(false);
        listView.setFocusable(false);
        if (mEmployee != null) {
            txtvRight.setText(mEmployee.name);
        }
        if (mRetailOrder != null) {
            txtvActionbarTitle.setText(mRetailOrder.code);
            txtvCustomerName.setText(mRetailOrder.customer_name);
//            txtvColor.setText(saleSendOrder.color);
//            txtvCount.setText(adapter.getTotalCount());
            getPackageList(mRetailOrder.id);
        }

        etScanCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_UP:
                            //发送请求
                            Log.i("addPackage","onEditorAction:"+actionId);
                            MUtils.hideSoftInput(SaleSendDetailActivity.this);
                            addPackage("");
                            return true;
                        default:
                            return true;
                    }
                }
                return false;
            }
        });
    }

    @OnClick(R.id.txtvLeft)
    public void onBackClickListener() {
        SaleSendDetailActivity.this.finish();
    }

    @OnClick(R.id.txtvConfirm)
    public void onConfirmClickListener() {
        MUtils.hideSoftInput(SaleSendDetailActivity.this);
        addPackage("");
    }

    @OnClick(R.id.btnSubmit)
    public void onSubmitClickListener() {
        if (adapter.getCount() == 0) {
            UIHelper.showLongToast(this, "该零售单无码单，无法提交审核");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否确认审核此零售单?");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                submitRetailOrder();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();

    }

    @OnClick(R.id.llFullVolume)
    public void onFullVolumeClickListener() {
        isFullVolume = !isFullVolume;
        if (isFullVolume) {
            txtvCheckImage.setBackgroundResource(R.mipmap.icon_check_checked);
        } else {
            txtvCheckImage.setBackgroundResource(R.mipmap.icon_check_normal);
        }
    }

    //添加出库单
    public void addPackage(String quantity) {
        String barcode = etScanCode.getText().toString().trim();
        if (TextUtils.isEmpty(barcode)) {
            UIHelper.showLongToast(this, "请输入条码");
            return;
        }

        if (!isFullVolume && TextUtils.isEmpty(quantity)) {
            inputDialog = new InputPackageNumDialog(SaleSendDetailActivity.this, "");
            inputDialog.show();
            inputDialog.setOnInputListener(new InputPackageNumDialog.OnInputListener() {
                @Override
                public void input(String value) {
                    addPackage(value);
                }

                @Override
                public void dismiss() {
                    requestScanFocus();
                }
            });
            return;
        }

        Api api = new Api(this, new OnNetRequest(this, true, "请稍等...") {
            @Override
            public void onSuccess(String msg) {
                RetailOrderPackBean responseBean = JSONUtils.fromJson(msg, RetailOrderPackBean.class);
                if (responseBean != null && responseBean.result != null) {
                    if (responseBean.result.size() == 0) {
                        UIHelper.showShortToast(SaleSendDetailActivity.this, "该零售单下无码单");
                    }
                    UIHelper.showShortToast(SaleSendDetailActivity.this, "出库成功");
                    adapter.refresh(responseBean.result);
                } else {
                    UIHelper.showShortToast(SaleSendDetailActivity.this, "该零售单下无码单");
                }
                txtvAllWeight.setText(adapter.getAllWeight());
                txtvCount.setText(adapter.getAllVolumeAndQuantity());
                etScanCode.setText("");
            }

            @Override
            public void onFail() {
                txtvAllWeight.setText(adapter.getAllWeight());
                txtvCount.setText(adapter.getAllVolumeAndQuantity());
                etScanCode.setText("");
            }
        });
        api.addPackage(mRetailOrder.id, isFullVolume ? "1" : "0", quantity, barcode);
    }

    //删除出库单
    public void deletePackage(String id) {
        Api api = new Api(this, new OnNetRequest(this, true, "请稍等") {
            @Override
            public void onSuccess(String msg) {
                RetailOrderPackBean responseBean = JSONUtils.fromJson(msg, RetailOrderPackBean.class);
                if (responseBean != null && responseBean.result != null) {
                    if (responseBean.result.size() == 0) {
                        UIHelper.showShortToast(SaleSendDetailActivity.this, "该零售单下无码单");
                    }
                    UIHelper.showShortToast(SaleSendDetailActivity.this, "删除成功");
                    adapter.refresh(responseBean.result);
                } else {
                    UIHelper.showShortToast(SaleSendDetailActivity.this, "该零售单下无码单");
                }
                txtvAllWeight.setText(adapter.getAllWeight());
                txtvCount.setText(adapter.getAllVolumeAndQuantity());
            }

            @Override
            public void onFail() {
                txtvAllWeight.setText(adapter.getAllWeight());
                txtvCount.setText(adapter.getAllVolumeAndQuantity());
            }
        });
        api.deletePackage(id);
    }


    //获取出库单
    public void getPackageList(String orderId) {
        Api api = new Api(this, new OnNetRequest(this, true, "正在加载...") {
            @Override
            public void onSuccess(String msg) {
                RetailOrderPackBean responseBean = JSONUtils.fromJson(msg, RetailOrderPackBean.class);
                if (responseBean != null && responseBean.result != null) {
                    if (responseBean.result.size() == 0) {
                        UIHelper.showShortToast(SaleSendDetailActivity.this, "该零售单下无码单");
                    }
                    adapter.refresh(responseBean.result);
                } else {
                    UIHelper.showShortToast(SaleSendDetailActivity.this, "该零售单下无码单");
                }
                txtvAllWeight.setText(adapter.getAllWeight());
                txtvCount.setText(adapter.getAllVolumeAndQuantity());
            }

            @Override
            public void onFail() {
                txtvAllWeight.setText(adapter.getAllWeight());
                txtvCount.setText(adapter.getAllVolumeAndQuantity());
            }
        });
        api.getRetailOrderPackList(orderId);
    }

    /**
     * 审核
     */
    public void submitRetailOrder() {
        Api api = new Api(this, new OnNetRequest(this, true, "正在提交审核...") {
            @Override
            public void onSuccess(String msg) {
                UIHelper.showShortToast(SaleSendDetailActivity.this, "成功提交审核");
                finish();
            }

            @Override
            public void onFail() {

            }
        });
        api.submitRetailOrder(mRetailOrder.id);
    }


    public void requestScanFocus() {
        etScanCode.requestFocus();
        etScanCode.setFocusable(true);
    }


    class PackageListAdapter extends BaseAdapter {
        List<RetailOrderPackBean.Result> packageBeanList = new ArrayList<>();

        public String getAllWeight() {
            BigDecimal allWeightBD = new BigDecimal("0");
            for (RetailOrderPackBean.Result result : packageBeanList) {
                String weight = result.weight;
                if (!TextUtils.isEmpty(weight)) {
                    BigDecimal weightBD = new BigDecimal(weight);
                    allWeightBD = allWeightBD.add(weightBD);
                }
            }
            String allW = MUtils.subZeroAndDot(allWeightBD.toString());
            return TextUtils.isEmpty(allW) ? "0" : allW + "公斤";
        }

        public String getAllVolumeAndQuantity() {
            BigDecimal allVolumeBD = new BigDecimal("0");
            BigDecimal allQuantityBD = new BigDecimal("0");
            for (RetailOrderPackBean.Result result : packageBeanList) {
                String volume = result.volume;
                if (!TextUtils.isEmpty(volume)) {
                    BigDecimal volumeBD = new BigDecimal(volume);
                    allVolumeBD = allVolumeBD.add(volumeBD);
                }
                String quantity = result.quantity;
                if (!TextUtils.isEmpty(quantity)) {
                    BigDecimal quantityBD = new BigDecimal(quantity);
                    allQuantityBD = allQuantityBD.add(quantityBD);
                }
            }
            String allV = MUtils.subZeroAndDot(allVolumeBD.toString());
            String allQ = MUtils.subZeroAndDot(allQuantityBD.toString());
            return TextUtils.isEmpty(allV) ? "0" : allV + "/" + (TextUtils.isEmpty(allQ) ? "0" : allQ);
        }


        public void refresh(List<RetailOrderPackBean.Result> ls) {
            if (ls == null) {
                ls = new ArrayList<>();
            }
            packageBeanList = ls;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return packageBeanList.size();
        }

        @Override
        public Object getItem(int position) {
            return packageBeanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_package, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final RetailOrderPackBean.Result packageBean = packageBeanList.get(position);
            viewHolder.txtvBarCode.setText(packageBean.barcode);
            viewHolder.txtvColor.setText(packageBean.color);
            viewHolder.txtvCraft.setText(packageBean.craft);
            viewHolder.txtvMaterial.setText(packageBean.material);
            viewHolder.txtvQuantity.setText(packageBean.quantity);
            viewHolder.txtvVolume.setText(packageBean.volume);

            viewHolder.txtvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String barCode = packageBean.barcode;
                    AlertDialog.Builder builder = new AlertDialog.Builder(SaleSendDetailActivity.this);
                    builder.setMessage("是否确认删除此发货码单\n" + barCode + "?");
                    builder.setTitle("提示");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String id = packageBean.id;
                            deletePackage(id);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
//                    UIHelper.showShortToast(SaleSendDetailActivity.this,position+"");
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        @Bind(R.id.txtvDelete)
        TextView txtvDelete;
        @Bind(R.id.txtvBarCode)
        TextView txtvBarCode;
        @Bind(R.id.txtvQuantity)
        TextView txtvQuantity;
        @Bind(R.id.txtvMaterial)
        TextView txtvMaterial;
        @Bind(R.id.txtvColor)
        TextView txtvColor;
        @Bind(R.id.txtvVolume)
        TextView txtvVolume;
        @Bind(R.id.txtvCraft)
        TextView txtvCraft;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    @Subscribe(priority = 2)
    public void OnScanResultEvent(final ScanResultEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (inputDialog == null || !inputDialog.isShowing()) {
                    etScanCode.setText(event.getResult());
                    addPackage("");
                }
            }
        });
    }
}
