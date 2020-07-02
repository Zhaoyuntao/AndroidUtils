package com.zhaoyuntao.androidutils.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zhaoyuntao.androidutils.R;
import com.zhaoyuntao.androidutils.tools.B;


/**
 * Created by zhaoyuntao on 2017/5/25.
 */

public class ZScaleBar extends View {

    private float percent = 0;
    //    private float radius_circle;
    private int w_view, h_view;
    private float w_line = 4;

    private float w_progress;
    private float radius_progress;
    private float position;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    private CallBack callBack;
    private float x_now, x_last;

    int color;
    int colorBack;

    public ZScaleBar(Context context) {
        super(context);
        init(null);
    }

    public ZScaleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ZScaleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ZScaleBar);//
            w_progress = typedArray.getDimension(R.styleable.ZScaleBar_ZScaleBar_w_progress, B.dip2px(getContext(), 10));
            radius_progress = typedArray.getDimension(R.styleable.ZScaleBar_ZScaleBar_roundradius, w_progress/2);
            color=typedArray.getColor(R.styleable.ZScaleBar_ZScaleBar_color,Color.parseColor("#61ca95"));
            colorBack=typedArray.getColor(R.styleable.ZScaleBar_ZScaleBar_colorback,Color.rgb(236, 236, 236));
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        w_view = getWidth();
        h_view = getHeight();
        if (w_view == 0 || h_view == 0) {
            return;
        }

        int x_start = (int) (radius_progress);
        float w_max = w_view - radius_progress * 2;
        position = x_start + radius_progress * 2 + (w_max - radius_progress * 2) * percent;

        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(w_line);
        p.setColor(colorBack);
        p.setStyle(Paint.Style.FILL);
        RectF rect = new RectF();
        rect.set(x_start, (int) (h_view / 2 - w_progress / 2), x_start + w_max, (int) (h_view / 2 + w_progress / 2));
        canvas.drawRoundRect(rect, radius_progress, radius_progress, p);


        RectF rect2 = new RectF();
        rect2.set(x_start, (int) (h_view / 2 - w_progress / 2), position, (int) (h_view / 2 + w_progress / 2));
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect2, radius_progress, radius_progress, p);

        p.setColor(colorBack);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        canvas.drawRoundRect(rect2, radius_progress, radius_progress, p);


//        p.setColor(Color.WHITE);
//        p.setStyle(Paint.Style.FILL);
//        canvas.drawCircle(position, y, radius_circle - w_line, p);
//        p.setColor(color);
//        p.setStyle(Paint.Style.STROKE);
//        p.setStrokeWidth(1);
//        canvas.drawCircle(position, y, radius_circle - w_line, p);
    }

    public void setPercent(float percent) {
        if (percent >= 0 && percent <= 1) {
            this.percent = percent;
            if (callBack != null) {
                callBack.whenScale(percent);
            }
            postInvalidate();
        }
    }

    public float getPercent() {
        return percent;
    }

    float min_move = 1;

    boolean pressOk;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        float max_position = w_view - radius_circle;
//        float min_position = radius_circle;
        float max_position = w_view;
        float min_position = 0;
        float tmp_position;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x_now = event.getX();
                float position = this.position;
                if (Math.abs(x_now - position) <= B.dip2px(getContext(), 20)) {
                    pressOk = true;
                }

                if (pressOk) {
                    x_last = x_now;
                    tmp_position = x_now;
                    if (tmp_position > max_position) {
                        percent = 1;
                    } else if (tmp_position < min_position) {
                        percent = 0;
                    } else {
                        percent = (tmp_position - min_position) / (max_position - min_position);
                    }
                    if (callBack != null) {
                        callBack.whenScaleStart(getPercent());
                    }
                } else {
                    x_now = -1;
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (pressOk) {
                    x_now = event.getX();
                    float distance_move = x_now - x_last;
                    if (Math.abs(distance_move) >= min_move) {
                        x_last = x_now;
                        tmp_position = x_now;
                        if (tmp_position > max_position) {
                            percent = 1;
                        } else if (tmp_position < min_position) {
                            percent = 0;
                        } else {
                            percent = (tmp_position - min_position) / (max_position - min_position);
                        }
                        if (callBack != null) {
                            callBack.whenScale(getPercent());
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (pressOk) {
                    x_now = event.getX();
                    tmp_position = x_now;
                    if (tmp_position > max_position) {
                        percent = 1;
                    } else if (tmp_position < min_position) {
                        percent = 0;
                    } else {
                        percent = (tmp_position - min_position) / (max_position - min_position);
                    }
                    if (callBack != null) {
                        callBack.whenScaleEnd(getPercent());
                    }
                }
                pressOk = false;
                break;
        }
        postInvalidate();
        return true;
    }

    public interface CallBack {
        void whenScaleStart(float percent);

        void whenScale(float percent);

        void whenScaleEnd(float percent);

    }
}
