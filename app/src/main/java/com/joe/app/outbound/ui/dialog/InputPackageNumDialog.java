package com.joe.app.outbound.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.joe.app.outbound.R;
import com.joe.app.outbound.ui.widget.ClearEditText;

import butterknife.Bind;

/**
 * Created by Joe on 2016/6/9.
 * Email-joe_zong@163.com
 */
public class InputPackageNumDialog extends Dialog {

    public InputPackageNumDialog(Context context) {
        super(context);
    }

    public InputPackageNumDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public InputPackageNumDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    private View view;
    ClearEditText etPackageNum;
    TextView txtvConfirm;
    private OnInputListener onInputListener;
    public InputPackageNumDialog(Context context, String currentValue) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = LayoutInflater.from(context).inflate(R.layout.dialog_input_package_num, null);
        etPackageNum = (ClearEditText)view.findViewById(R.id.etPackageNum);
        txtvConfirm = (TextView)view.findViewById(R.id.txtvConfirm);
        etPackageNum.setText(currentValue);
        this.setContentView(view);
        setClickListeners();

        Display d = ((Activity)context).getWindowManager().getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.3); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.65
        getWindow().setAttributes(p);

    }

    public void setClickListeners(){
        txtvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = etPackageNum.getText().toString().trim();
                if(onInputListener!=null) {
                    onInputListener.input(value);
                }
                dismiss();
            }
        });

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(onInputListener!=null){
                    onInputListener.dismiss();
                }
            }
        });
    }

    public void setOnInputListener(OnInputListener listener){
        this.onInputListener = listener;
    }

    public interface OnInputListener{
        void input(String value);
        void dismiss();
    }
}
