package com.zhaoyuntao.androidutils.component;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.zhaoyuntao.androidutils.tools.B;

public class ZCircleView extends View {


    //加载动画相关
    private PathMeasure mPathMeasureOfLoadUI;
    private ValueAnimator animatorOfLoadUI;
    private float[] mPointOfLoadUI;
    private float[] mTanOfLoadUI;
    //加载动画的path
    private Path path_rect_loadUI;
    private float w_border_loadUI;

    float w_view;
    float h_view;

    float radius;

    float black;
    float blue;

    public ZCircleView(Context context) {
        super(context);
        init();
    }

    public ZCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        black = B.dip2px(getContext(), 5);
        blue = B.dip2px(getContext(), 5);
        w_border_loadUI = B.dip2px(getContext(), 8);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (w_view == 0 || h_view == 0) {
            w_view = getWidth();
            h_view = getHeight();
        }

        if (w_view <= 0 || h_view <= 0) {
            return;
        }

        radius = w_view / 2 - B.dip2px(getContext(), 5);

        float x = w_view / 2f;
        float y = h_view / 2f;
        if (path_rect_loadUI == null) {
            path_rect_loadUI = getLoadUiPath();
            startAnimOfLoadUI(path_rect_loadUI);
        }
        Paint paint = new Paint();
        //先创建一个渲染器
        SweepGradient mSweepGradient = new SweepGradient(x, y, new int[]{Color.argb(0, 0, 0, 0), Color.argb(255, 0, 0, 255)}, null);
        float degrees2 = (float) (Math.atan2(x - w_view / 2f, y - h_view / 2f) * 180f / Math.PI) - 90;//渐变旋转的角度
        Matrix matrix = new Matrix();
        matrix.setRotate(-degrees2, w_view / 2f, h_view / 2f);
        mSweepGradient.setLocalMatrix(matrix);//设置旋转矩阵
        paint.setShader(mSweepGradient);//中心渐变
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);//抗锯齿
        paint.setStrokeWidth(w_border_loadUI);

        paint.setPathEffect(new DashPathEffect(new float[]{black, blue}, 0));
        paint.setColor(Color.BLUE);
        // 将tan值通过反正切函数得到对应的弧度，在转化成对应的角度度数
        float degrees = (float) (Math.atan2(mTanOfLoadUI[1], mTanOfLoadUI[0]) * 180f / Math.PI) + 90;
        canvas.save();
        canvas.rotate(degrees, x, y);
        canvas.drawPath(path_rect_loadUI, paint);
        canvas.restore();

    }

    /**
     * 获取加载动画的Path
     *
     * @return
     */
    private Path getLoadUiPath() {

        //the path
        Path path_border = new Path();

        path_border.addCircle(w_view / 2, h_view / 2, radius, Path.Direction.CW);

        //闭合路径
        path_border.close();
        return path_border;
    }

    /**
     * 开启加载动画
     *
     * @param path
     */
    private void startAnimOfLoadUI(Path path) {
        stopLoadUI();
        if (mPathMeasureOfLoadUI == null) {
            mPathMeasureOfLoadUI = new PathMeasure(path, false);
        }
        if (mPointOfLoadUI == null) {
            mPointOfLoadUI = new float[2];
        }
        if (mTanOfLoadUI == null) {
            mTanOfLoadUI = new float[2];
        }
        if (animatorOfLoadUI == null) {
            animatorOfLoadUI = ValueAnimator.ofFloat(0, mPathMeasureOfLoadUI.getLength());
            animatorOfLoadUI.setDuration(1700);
            animatorOfLoadUI.setInterpolator(new LinearInterpolator()); //插值器
            animatorOfLoadUI.setRepeatCount(ValueAnimator.INFINITE);
            animatorOfLoadUI.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float distance = (float) animation.getAnimatedValue();
                    mPathMeasureOfLoadUI.getPosTan(distance, mPointOfLoadUI, mTanOfLoadUI);
                    postInvalidate();
                }
            });
        }
        if (!animatorOfLoadUI.isRunning()) {
            animatorOfLoadUI.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animatorOfLoadUI.start();
        }
    }

    /**
     * 关闭加载动画
     */
    private void stopLoadUI() {
        if (animatorOfLoadUI != null && animatorOfLoadUI.isRunning()) {
            animatorOfLoadUI.cancel();
            animatorOfLoadUI.end();
        }
    }

    @Override
    public void destroyDrawingCache() {
        stopLoadUI();
        super.destroyDrawingCache();
    }
}
