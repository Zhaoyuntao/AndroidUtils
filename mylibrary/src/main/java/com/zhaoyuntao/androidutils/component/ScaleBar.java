package com.zhaoyuntao.androidutils.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zhaoyuntao.androidutils.tools.B;


/**
 * Created by zhaoyuntao on 2017/5/25.
 */

public class ScaleBar extends View {

    private float position = -1;
    private float radius_circle;
    private int w_view, h_view;
    private float w_line = 4;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    private CallBack callBack;
    private float x_now, y_now, x_last, y_last;

    public ScaleBar(Context context) {
        super(context);
        init(context);
    }

    public ScaleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScaleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        w_view = getWidth();
        h_view = getHeight();
        if (w_view == 0 || h_view == 0) {
            return;
        }
        float radius_dp = 10;
        radius_circle = B.dip2px(getContext(), radius_dp);
        float w_progress = B.dip2px(getContext(), 2);
        if (position == -1) {
            position = radius_circle;
        }
        float y = h_view / 2;
        int color = Color.argb(30, 0, 0, 0);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStrokeWidth(w_line);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        RectF rect = new RectF();
        rect.set(radius_circle / 2, (int) (h_view / 2 - w_progress / 2), w_view - radius_circle / 2, (int) (h_view / 2 + w_progress / 2));
        canvas.drawRoundRect(rect, 0, h_view / 2 - w_progress / 2, p);


        RectF rect2 = new RectF();
        rect2.set(radius_circle / 2, (int) (h_view / 2 - w_progress / 2), position, (int) (h_view / 2 + w_progress / 2));
        p.setColor(Color.parseColor("#61ca95"));
        p.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect2, 0, h_view / 2 - w_progress / 2, p);

        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        canvas.drawRoundRect(rect, 0, h_view / 2 - w_progress / 2, p);

        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        canvas.drawCircle(position, y, radius_circle - w_line, p);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        canvas.drawCircle(position, y, radius_circle - w_line, p);
//        canvas.drawCircle(position, y, radius_circle / 3, p);
    }

    public void setPercent(float percent) {
        if (percent >= 0 && percent <= 1) {
            this.position = (w_view - radius_circle * 2) * percent + radius_circle;
            if(callBack!=null){
                callBack.whenScale(getPercent());
            }
            postInvalidate();
        }
    }

    private float getPercent() {
        if (w_view == 0 || h_view == 0 || w_view <= h_view) {
            return 0;
        }
        float percent = (position - radius_circle) / (w_view - radius_circle * 2);
        return percent;
    }

    float position_old;
    float min_move = 5;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x_now = event.getX();
                y_now = event.getY();
                x_last = x_now;
                y_last = y_now;

                if (callBack != null) {
                    callBack.whenScaleStart(getPercent());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                position_old=position;
                x_now = event.getX();
                y_now = event.getY();
                float distance_move = x_now - x_last;
                if (Math.abs(distance_move) >= min_move) {
                    x_last = x_now;
                    y_last = y_now;
                    float tmp_position = position_old + distance_move;
                    float max_position = w_view - radius_circle;
                    float min_position = radius_circle;
                    if (tmp_position > max_position) {
                        position = max_position;
                    } else if (tmp_position < min_position) {
                        position = min_position;
                    } else {
                        position = tmp_position;
                    }
                    position_old = position;
                    if (callBack != null) {
                        callBack.whenScale(getPercent());
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (callBack != null) {
                    callBack.whenScaleEnd(getPercent());
                }
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
