package com.zhaoyuntao.androidutils.component;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.zhaoyuntao.androidutils.R;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;

/**
 * Created by feijie.xfj on 17/11/19.
 */

public class FloatWindowHelper {
    private WindowManager windowManager;

    private Application application;

    private View windowView;

    private FrameLayout container;

    private WindowManager.LayoutParams params;

    private int width, height;

    private static FloatWindowHelper floatWindowHelper;

    private final float propertion = 0.8f;

    public static FloatWindowHelper getInstance(Activity activity) {
        if (floatWindowHelper == null) {
            synchronized (FloatWindowHelper.class) {
                if (floatWindowHelper == null) {
                    floatWindowHelper = new FloatWindowHelper(activity);
                }
            }
        }
        return floatWindowHelper;
    }


    private boolean isSmall = false;
    private Context context;

    private FloatWindowHelper(Activity activity) {
        this.application = activity.getApplication();
        this.context = activity;
        initWindowView();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPercent(float percent) {
        int[] whScreen = B.getScreenWH(application);
        double w_screen = whScreen[0];
        double h_screen = whScreen[1];

        double propertionScreen = w_screen / h_screen;
        if (propertionScreen > propertion) {
            height = (int) (h_screen * percent);
            width = (int) (height / propertion);
        } else {
            width = (int) (w_screen * percent);
            height = (int) (width * propertion);
        }
    }

    private void initWindowView() {
        if (windowView == null) {
            windowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
            LayoutInflater inflater = LayoutInflater.from(context);
            windowView = inflater.inflate(R.layout.layout_float_window_view, null);
            container = windowView.findViewById(R.id.container);
            windowView.setOnTouchListener(new WindowTouchListener());
            setCloseListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    if (!isSmall) {
                        miniSize();
                        windowView.findViewById(R.id.close).setBackgroundResource(R.drawable.logviewresume);
                    } else {
                        normalSize();
                        windowView.findViewById(R.id.close).setBackgroundResource(R.drawable.logviewclose);
                    }
                }
            });
        }
    }

    private OnSizeChangedListener onSizeChangedListener;

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        this.onSizeChangedListener = onSizeChangedListener;
    }

    public interface OnSizeChangedListener {
        void whenMiniSize();

        void whenNormalSize();
    }

    private WindowManager.LayoutParams initWindowParams() {
        if (params == null) {
            params = new WindowManager.LayoutParams();
            params.format = PixelFormat.RGBA_8888;
            //兼容7.1系统
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

//            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//7.1系统走这里
//                params.type = WindowManager.LayoutParams.TYPE_PHONE;
//            } else {//不知道为什么 MIUI9.7.11.9开发板系统 不支持用Toast
//                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//            }
//        } else {
//            params.type = WindowManager.LayoutParams.TYPE_PHONE;
//        }
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        }
        params.gravity = Gravity.LEFT | Gravity.TOP;
        int[] whScreen = B.getScreenWH(application);
        double w_screen = whScreen[0];
        double h_screen = whScreen[1];

        if (width == 0) {
            width = (int) (w_screen * propertion);
        }
        if (height == 0) {
            height = (int) (w_screen * propertion * propertion);
        }

        if (isSmall) {
            params.width = B.dip2px(context, 25);
            params.height = B.dip2px(context, 60);
            if (x_miniSize == defaultView && y_miniSize == defaultView) {
                params.x = (int) (w_screen - params.width - B.dip2px(application, 20));
                params.y = (int) (h_screen - params.height) / 2;
            } else {
                params.x = x_miniSize;
                params.y = y_miniSize;
            }
        } else {
            params.width = width;
            params.height = height;
            if (x_normalSize == defaultView && y_normalSize == defaultView) {
                params.x = (int) (w_screen - params.width - B.dip2px(application, 20));
                params.y = (int) (h_screen - params.height) / 2;
            } else {
                params.x = x_normalSize;
                params.y = y_normalSize;
            }
        }

        return params;
    }

    public boolean judgePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            return true;
        }
    }

    public void requestPermission(final Context context) {
        //如果没有权限
        if (!judgePermission()) {
            new ZDialog(context).setTitle("开启权限").setMessage("请打开日志权限").setTouchCancelable(true).setButton1OnClickListener("取消", new ZDialog.OnClickListener() {
                @Override
                public void onClick(ZDialog dialog) {
                    dialog.cancel();
                }
            }).setButton2OnClickListener("前往设置", new ZDialog.OnClickListener() {
                @Override
                public void onClick(ZDialog dialog) {
                    dialog.cancel();
                    context.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName())));
                }
            }).show();
        }
    }

    public void show() {
        normalSize();
    }

    /**
     * 最小化
     */
    public void miniSize() {
        if (!judgePermission()) {
            return;
        }
        isSmall = true;
        initWindowParams();
        if (windowView != null && windowView.getParent() != null) {
            container.setVisibility(View.GONE);
            windowManager.updateViewLayout(windowView, params);
            if (onSizeChangedListener != null) {
                onSizeChangedListener.whenMiniSize();
            }
        }
    }

    /**
     * 最大化
     */
    public void normalSize() {
        if (!judgePermission()) {
            return;
        }
        isSmall = false;
        initWindowParams();
        container.setVisibility(View.VISIBLE);
        if (windowView.getParent() == null) {
            windowManager.addView(windowView, params);
        } else {
            windowManager.updateViewLayout(windowView, params);
        }
        if (onSizeChangedListener != null) {
            onSizeChangedListener.whenNormalSize();
        }
    }

    /**
     * 关闭
     */
    public void cancel() {
        if (windowView != null && windowView.getParent() != null) {
            windowManager.removeViewImmediate(windowView);
        }
    }


    public void setDownListener(View.OnClickListener onClickListener) {
        if (windowView == null) {
            initWindowView();
        }
        if (windowView != null) {
            windowView.findViewById(R.id.down).setOnClickListener(onClickListener);
        }
    }

    public void setCloseListener(View.OnClickListener onClickListener) {
        if (windowView == null) {
            initWindowView();
        }
        if (windowView != null) {
            windowView.findViewById(R.id.close).setOnClickListener(onClickListener);
        }
    }

    public void setContentView(View contentView) {
        if (contentView != null) {
            contentView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (windowView == null) {
                initWindowView();
            }
            container.removeAllViews();
            container.addView(contentView);
        } else {
            S.ee("contentView is null");
        }
    }

    private final int defaultView = -1000000;
    //保存正常大小下的坐标
    private int x_normalSize = defaultView, y_normalSize = defaultView;
    //保存迷你坐标下的坐标
    private int x_miniSize = defaultView, y_miniSize = defaultView;

    private int mode = -1;

    private static final int DOWN = 0;

    //开始触控的坐标，相对于屏幕左上角
    private float x_start, y_start;

    private class WindowTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mode = DOWN;
                    x_start = event.getRawX();
                    y_start = event.getRawY();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (mode == DOWN) {
                        float mTouchCurrentX = event.getRawX();
                        float mTouchCurrentY = event.getRawY();
                        if (params != null) {

                            params.x += mTouchCurrentX - x_start;
                            params.y += mTouchCurrentY - y_start;

                            if (isSmall) {
                                x_miniSize = params.x;
                                y_miniSize = params.y;
                            } else {
                                x_normalSize = params.x;
                                y_normalSize = params.y;
                            }
                            if (windowView != null && windowView.getParent() != null) {
                                windowManager.updateViewLayout(windowView, params);
                            }
                        }
                        x_start = mTouchCurrentX;
                        y_start = mTouchCurrentY;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    mode = -1;
                    break;
                }
            }
            return false;
        }
    }

}
