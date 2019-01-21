package com.zhaoyuntao.androidutils.component;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.zhaoyuntao.androidutils.R;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.VibratorTool;
import com.zhaoyuntao.androidutils.tools.ZThread;

public class FingerControl extends View {

    public CallBack callBack;
    // 摇杆初始坐标,view的中心
    private float x_center, y_center;
    // 触点绘制半径
    private float radius_pointDraw;
    //触点真实半径
    private float radius_pointCircle;
    // 背景圆形的宽度
    int widthCircle;
    //view的尺寸
    protected int w_view, h_view;
    //产生底盘控制指令的频率
    public float frame = 5;
    //产生地盘控制指令的线程
    private ZThread thread;
    //背景图片
    private Bitmap bitmap_back;
    //摇杆图片
    private Bitmap bitmap_pointer;
    //光效图片
    private Bitmap bitmap_light;
    //手指触点
    private float x_pointer, y_pointer;
    //旋转角度
    private float degree;

    private float xMax = 100;
    private float yMax = 100;

    private Context context;

    public FingerControl(Context context) {
        super(context);
        init(context);
    }

    public FingerControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FingerControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        if (thread != null) {
            thread.close();
        }
        thread = new ZThread(frame) {
            @Override
            protected void todo() {
                if (callBack != null) {
                    callBack.send(getX_pointer(), getY_pointer());
                }
            }
        };
        thread.pauseThread();
        thread.start();
        bitmap_back = B.getBitmapById(context, R.drawable.background_fingercontrol);
        bitmap_light = B.getBitmapById(context, R.drawable.background_fingercontrol_light);
        bitmap_pointer = B.getBitmapById(context, R.drawable.button_fingercontrol);
    }

    /**
     * 设置发生频率
     *
     * @param frame
     */
    public void setFrame(float frame) {
        this.frame = frame;
        if (thread != null) {
            thread.setFrame(frame);
        }
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        w_view = getWidth();
        h_view = getHeight();
        if (w_view == 0 || h_view == 0) {
            return;
        }
        radius_pointDraw = w_view > h_view ? h_view / 5f : w_view / 5f;
        radius_pointCircle= w_view > h_view ? h_view / 7f : w_view / 7f;
        x_center = w_view / 2f;
        y_center = h_view / 2f;
        if (x_pointer == 0 || y_pointer == 0) {
            x_pointer = x_center;
            y_pointer = y_center;
        }
        canvas.save();
        canvas.scale(0.8f, 0.8f, x_center, y_center);

        widthCircle = (int) (w_view > h_view ? y_center : x_center);
        Paint paint = new Paint();
        RectF rec_oval = new RectF();
        rec_oval.set(0, 0, (float) w_view, (float) h_view);
        paint.setAntiAlias(true);

        //绘制背景图片
        Rect rect_src = new Rect();
        rect_src.set(0, 0, bitmap_back.getWidth(), bitmap_back.getHeight());
        Rect rect_des = new Rect();

        rect_des.set((int) (x_center - widthCircle), (int) (y_center - widthCircle), (int) (x_center + widthCircle), (int) (y_center + widthCircle));
        canvas.drawBitmap(bitmap_back, rect_src, rect_des, paint);

        //绘制中心按钮
        Rect rec_des_pointer = new Rect();
        rec_des_pointer.set((int) (-radius_pointDraw + x_pointer), (int) (-radius_pointDraw + y_pointer), (int) (radius_pointDraw + x_pointer), (int) (radius_pointDraw + y_pointer));
        Rect rect_src_pointer = new Rect();
        float w_pointer = bitmap_pointer.getWidth() / 2f;
        float h_pointer = bitmap_pointer.getHeight() / 2f;
        rect_src_pointer.set((int) (x_center - w_pointer), (int) (y_center - h_pointer), (int) (x_center + bitmap_pointer.getWidth()), (int) (y_center + bitmap_pointer.getHeight()));
        canvas.drawBitmap(bitmap_pointer, rect_src, rec_des_pointer, paint);

        //绘制光
        Rect rect_src1 = new Rect();
        rect_src1.set(0, 0, bitmap_light.getWidth(), bitmap_light.getHeight());
        canvas.save();
        canvas.scale(1.26f, 1.26f, x_center, y_center);
        canvas.rotate(degree, x_center, y_center);
        canvas.drawBitmap(bitmap_light, rect_src1, rect_des, paint);
        canvas.restore();
        canvas.restore();
    }


    public float getX_pointer() {
        float distanceXMax = this.x_center - radius_pointCircle;
        return (x_pointer - this.x_center) / distanceXMax * xMax;
    }

    public float getY_pointer() {
        float distanceYMax = this.y_center - radius_pointCircle;
        return (y_pointer - this.y_center) / distanceYMax * yMax;
    }

    float radius_max;
    float radius_press;
    float radius_now;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x_pressTmp = event.getX();
        float y_pressTmp = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                S.s("FingerControl:down");
                if (context instanceof Activity) {
                    VibratorTool.vibrate((Activity) context, 10);
                }
                radius_press = (float) Math.sqrt(Math.pow(x_pressTmp - x_center, 2) + Math.pow(y_pressTmp - y_center, 2));
                radius_max = (w_view > h_view ? y_center : x_center) - radius_pointCircle;
                if (thread != null && radius_press <= radius_max) {
                    thread.resumeThread();
                }
            case MotionEvent.ACTION_MOVE:
                //只有按下区域落在园内时,才生效
                if (radius_press <= radius_max) {
                    //计算角度
                    if (x_pressTmp == x_center && y_pressTmp == y_center) {
                        degree = 0;
                    } else {
                        //当前移动半径
                        radius_now = (float) Math.sqrt(Math.pow(x_pressTmp - x_center, 2) + Math.pow(y_pressTmp - y_center, 2));

                        //最大移动半径
                        float x_distance = x_pressTmp - x_center;
                        float y_distance = y_pressTmp - y_center;
                        degree = (float) Math.toDegrees(Math.atan(y_distance / x_distance));
                        if (x_distance < 0 && y_distance < 0) {
                            degree -= 90;
                        } else if (x_distance > 0 && y_distance > 0) {
                            degree += 90;
                        } else if (x_distance < 0 && y_distance > 0) {
                            degree -= 90;
                        } else {
                            degree += 90;
                        }
                    }
                    if (radius_now < radius_max) {
                        this.x_pointer = x_pressTmp;
                        this.y_pointer = y_pressTmp;
                    } else {
                        this.x_pointer = x_center + (float) (radius_max * Math.cos(Math.toRadians(degree - 90)));
                        this.y_pointer = y_center + (float) (radius_max * Math.sin(Math.toRadians(degree - 90)));
                        radius_now = radius_max;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                S.s("FingerControl:up");
//                this.x_pointer = x_center;
//                this.y_pointer = y_center;
                startWaveAnimation(radius_now, degree);
                if (thread != null) {
                    thread.pauseThread();
                }
                break;
        }
        postInvalidate();
        return true;
    }

    //动画相关
    ValueAnimator waveAnimator;

    private void stopAnimation() {
        if (waveAnimator != null && waveAnimator.isRunning()) {
            waveAnimator.cancel();
            waveAnimator = null;
        }
    }

    public void startWaveAnimation(final float radius, final float angle) {
        stopAnimation();
        final int range = 50;
        waveAnimator = ValueAnimator.ofInt(range, 0);
        waveAnimator.setDuration(100);
        waveAnimator.setRepeatMode(ValueAnimator.RESTART);
        waveAnimator.setRepeatCount(0);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                if (value == 0) {
                    x_pointer = x_center;
                    y_pointer = y_center;
                    degree = 0;
                } else {
                    float radiusTmp = value / (float) range * radius;
                    radiusTmp = (float) Math.sqrt(radiusTmp);
                    x_pointer = x_center + (float) (radiusTmp * Math.cos(Math.toRadians(angle - 90)));
                    y_pointer = y_center + (float) (radiusTmp * Math.sin(Math.toRadians(angle - 90)));
                }
//                S.s("x:"+value);
                postInvalidate();
            }
        });
        waveAnimator.start();
    }

    @Override
    public void destroyDrawingCache() {
        destroyThread();
        stopAnimation();
        super.destroyDrawingCache();
    }


    public void destroyThread() {
        if (thread != null) {
            thread.close();
        }
    }

    public interface CallBack {
        void send(float x, float y);
    }
}
