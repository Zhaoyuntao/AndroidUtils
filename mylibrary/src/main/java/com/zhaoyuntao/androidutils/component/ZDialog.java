package com.zhaoyuntao.androidutils.component;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoyuntao.androidutils.R;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;


public class ZDialog extends Dialog {

    /* fields */

    private Object customContext = null;

    private Context context;
    private int w_view;

    private View frameLayout;
    private LinearLayout container_title;
    private FrameLayout container_content;

    private ZButton zButton_1, zButton_2, zButton_3;


    /* methods: Dialog */

    public ZDialog(Context context) {
        super(context, R.style.ASlideDialog);
        this.context = context;
        w_view = B.getScreenWH(context)[0];
        frameLayout = LayoutInflater.from(context).inflate(R.layout.layout_dialog, null);
        container_content = (FrameLayout) frameLayout.findViewById(R.id.container);
        container_title = (LinearLayout) frameLayout.findViewById(R.id.title);
        zButton_1 = frameLayout.findViewById(R.id.bt_1);
        zButton_2 = frameLayout.findViewById(R.id.bt_2);
        zButton_3 = frameLayout.findViewById(R.id.bt_3);
        zButton_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZDialog.this.cancel();
            }
        });
        getWindow().setWindowAnimations(R.style.ASlideDialog);
        setContentView(frameLayout, new FrameLayout.LayoutParams(w_view, ViewGroup.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.BOTTOM);
    }

    public void setCustomContext(Object customContext) {
        this.customContext = customContext;
    }

    public Object getCustomContext() {
        return this.customContext;
    }

    public ZDialog setTouchCancelable(boolean ok) {
        setCanceledOnTouchOutside(ok);
        return this;
    }

    public ZDialog setTitle(String text) {
        container_title.removeAllViews();
        TextView textView = new TextView(context);
        int color = ContextCompat.getColor(context,R.color.black_70_transparent);
        textView.setTextColor(color);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, B.dip2px(context, 20));
        lp.gravity = Gravity.CENTER;
        textView.setLayoutParams(lp);
        textView.setTextSize(15);
        textView.setText(text);
        container_title.addView(textView);

        return this;
    }

    public ZDialog setGravity(int gravity) {
        this.getWindow().setGravity(gravity);
        return this;
    }

    public ZDialog setChildView(View view) {
        container_content.removeAllViews();
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        container_content.addView(view);
        return this;
    }
    public ZDialog setMessage(String text,int gravity){
        setText(text,gravity);
        return this;
    }
    public ZDialog setMessage(String text){
        setText(text,Gravity.CENTER);
        return this;
    }
    public ZDialog setText(String text,int gravity) {
        if (S.isEmpty(text)) {
            return this;
        }
        container_content.removeAllViews();
        TextView textView = new TextView(context);
        textView.setTextColor( ContextCompat.getColor(context,R.color.black_30_transparent));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.LEFT;
        lp.setMargins(80, 20, 80, 40);
        textView.setLayoutParams(lp);
        textView.setText(text);
//        textView.setTextSize(14);
        textView.setGravity(gravity);
        container_content.addView(textView);
        return this;
    }

    public ZDialog setButton1OnClickListener(final OnClickListener onClickListener) {
        View.OnClickListener onClickListener1=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(ZDialog.this);
            }
        };
        zButton_1.setOnClickListener(onClickListener1);
        zButton_1.setVisibility(View.VISIBLE);
        return this;
    }
    public ZDialog setButton1OnClickListener(String text,OnClickListener onClickListener) {
        zButton_1.setText_center(text);
        setButton1OnClickListener(onClickListener);
        return this;
    }

    public ZDialog setButton2OnClickListener(final OnClickListener onClickListener) {
        View.OnClickListener onClickListener1=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(ZDialog.this);
            }
        };
        zButton_2.setOnClickListener(onClickListener1);
        zButton_2.setVisibility(View.VISIBLE);
        return this;
    }
    public ZDialog setButton2OnClickListener(String text,OnClickListener onClickListener) {
        zButton_2.setText_center(text);
        setButton2OnClickListener(onClickListener);
        return this;
    }

    public ZDialog setButton3OnClickListener(final OnClickListener onClickListener) {
        View.OnClickListener onClickListener1=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(ZDialog.this);
            }
        };
        zButton_3.setOnClickListener(onClickListener1);
        zButton_3.setVisibility(View.VISIBLE);
        return this;
    }
    public ZDialog setButton3OnClickListener(String text,OnClickListener onClickListener) {
        zButton_3.setText_center(text);
        setButton3OnClickListener(onClickListener);
        return this;
    }
    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    public interface OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param dialog The view that was clicked.
         */
        void onClick(ZDialog dialog);
    }
}
