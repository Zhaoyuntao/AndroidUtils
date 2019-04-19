package com.zhaoyuntao.androidutils.component;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.zhaoyuntao.androidutils.R;


/**
 * 复制弹出窗。
 *
 * @author hj
 * @date 2016年7月29日 上午9:54:12
 */
public class ClipPopupWindow extends PopupWindow {
    public interface ClipListener {
        void onCopy(String text);

        void onCancel();
    }

    private ClipListener mClipListener;

    private boolean mIsCopyed;

    private Context mContext;

    private String mText;

    public ClipPopupWindow(Context context) {
        super(LayoutInflater.from(context).inflate(R.layout.popup_window_clip, null), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        mContext = context;

        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        getContentView().setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mClipListener) {
                    ClipboardManager cb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    cb.setText(mText);

                    mClipListener.onCopy(mText);
                    mIsCopyed = true;
                }

                if (isShowing()) {
                    dismiss();
                }
            }
        });

        setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                if (null != mClipListener) {
                    if (!mIsCopyed) {
                        mClipListener.onCancel();
                    }
                }
            }
        });
    }

    public void setClipListener(ClipListener listener) {
        mClipListener = listener;
    }

    public void setCopyText(String text) {
        mText = text;
    }

}
