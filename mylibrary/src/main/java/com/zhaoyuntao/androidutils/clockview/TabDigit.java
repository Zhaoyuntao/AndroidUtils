package com.zhaoyuntao.androidutils.clockview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.zhaoyuntao.androidutils.R;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.TextMeasure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabDigit extends View {

    private int index_now;
    private int index_next;
    /*
     * false: rotate upwards
     * true: rotate downwards
     */
    private boolean mReverseRotation = true;

    Camera camera = new Camera();

    private Page mTopPage;

    private Page mBottomPage;

    private Page mMiddlePage;

    private List<Page> pages = new ArrayList<>(3);

//    private AbstractTabAnimation tabAnimation;

    private ValueAnimator animator;

    private Matrix mProjectionMatrix = new Matrix();

    private int mCornerSize;

    private int textSize = -1;
    private int textColor = 1;
    private int backgroundColor = 1;

    private Paint textPaint;

    private Paint mDividerPaint;

    private Paint mBackgroundPaint;

//    private Rect mTextMeasured = new Rect();

    private int mPadding = 0;

    private char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public TabDigit(Context context) {
        this(context, null);
    }

    public TabDigit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabDigit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TabDigit, 0, 0);

        mPadding = ta.getDimensionPixelSize(R.styleable.TabDigit_padding, B.dip2px(getContext(), 15));
        textSize = ta.getDimensionPixelSize(R.styleable.TabDigit_textSize, B.sp2px(getContext(), 60));
        mCornerSize = ta.getDimensionPixelSize(R.styleable.TabDigit_cornerSize, B.dip2px(getContext(), 5));
        textColor = ta.getColor(R.styleable.TabDigit_textColor, Color.WHITE);
        backgroundColor = ta.getColor(R.styleable.TabDigit_backgroundColor, Color.BLACK);
        mReverseRotation = ta.getBoolean(R.styleable.TabDigit_reverseRotation, true);

        ta.recycle();
        initPaints();

        initTabs();
    }

    private void initPaints() {
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mDividerPaint.setColor(Color.WHITE);
        mDividerPaint.setStrokeWidth(1);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);

        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);

        mBackgroundPaint.setColor(backgroundColor);
    }

    private void initTabs() {
        // top Page
        mTopPage = new Page(Page.UPPER_POSITION);
        mTopPage.rotate(180);
        pages.add(mTopPage);

        // bottom Page
        mBottomPage = new Page(Page.LOWER_POSITION);
        pages.add(mBottomPage);

        // middle Page
        mMiddlePage = new Page(Page.MIDDLE_POSITION);
        pages.add(mMiddlePage);

//        tabAnimation = mReverseRotation ? new TabAnimationDown(mTopPage, mBottomPage, mMiddlePage) : new TabAnimationUp(mTopPage, mBottomPage, mMiddlePage);
//
//        tabAnimation.initMiddleTab();

        setIndex(0, false);
    }

    /**
     * 开启目的地图标动画
     */
    private void startAnim(long during) {
        if (animator != null && animator.isRunning()) {
            animator.end();
        } else {

            animator = ValueAnimator.ofInt(0, 180);
            animator.setDuration(during);
            animator.setInterpolator(new LinearInterpolator()); //插值器
//            animator.setRepeatCount(ValueAnimator.INFINITE);
//            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                boolean top = false;
                boolean mi = false;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) (animation.getAnimatedValue());
                    S.s("va:               " + value);
                    if (value <= 180 && !top) {
                        top = true;
                        mTopPage.next();
                    } else if (value <= 90 && !mi) {
                        mi = true;
                        mMiddlePage.next();
                    } else if (value <= 0) {
                        top = false;
                        mi = false;
                        mBottomPage.next();
                    }
                    mMiddlePage.rotate(value);
                    flush();
//                    ViewCompat.postOnAnimationDelayed(this, this, 40);
//                    flush();
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
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
//                    S.s("===========================================");
//                    mMiddlePage.next();
//                    mTopPage.next();
//                    mBottomPage.next();
//                    flush();
                }
            });
        }
        if (!animator.isRunning()) {
            animator.start();
        }
    }

    /**
     * 关闭目标点动画
     */
    private void stopAnimator() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
            animator.end();
        }
    }

    /**
     * 设置一组char
     *
     * @param array
     */
    public void setCharArray(char... array) {
        if (array != null && array.length > 0) {
            this.chars = array;
        }
    }

    public void setIndex(int index, boolean showAnim) {
        if (index >= 0 && index < chars.length) {
            this.index_now = index;
            this.index_next = index_now + 1;
            if (this.index_next >= chars.length) {
                this.index_next = 0;
            }
            if (showAnim) {
                startAnim(1000);
            } else {
                for (Page page : pages) {
                    page.setChar(index);
                }
                flush();
            }
        }
    }

    public void setNextIndex() {
        setIndex(index_next, true);
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        calculateTextSize(mTextMeasured);
//
//        int childWidth = mTextMeasured.width() + mPadding;
//        int childHeight = mTextMeasured.height() + mPadding;
//        for (Page page : pages) {
//            page.measure(childWidth, childHeight);
//        }
//
//        int maxChildWidth = mMiddlePage.maxWidth();
//        int maxChildHeight = 2 * mMiddlePage.maxHeight();
//        int resolvedWidth = resolveSize(maxChildWidth, widthMeasureSpec);
//        int resolvedHeight = resolveSize(maxChildHeight, heightMeasureSpec);
//
//        setMeasuredDimension(resolvedWidth, resolvedHeight);
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mProjectionMatrix.reset();
            int centerY = getHeight() / 2;
            int centerX = getWidth() / 2;
            MatrixHelper.translate(mProjectionMatrix, centerX, -centerY, 0);
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
//        play();
//        startAnim();
        return super.onCreateDrawableState(extraSpace);
    }

    @Override
    public void destroyDrawingCache() {
        stopAnimator();
        super.destroyDrawingCache();
    }

    private void calculateTextSize(Rect rect) {
        textPaint.getTextBounds("8", 0, 1, rect);
    }

    public void setTextSize(int size) {
        textPaint.setTextSize(size);
        requestLayout();
    }

    public int getTextSize() {
        return (int) textPaint.getTextSize();
    }

    public void setPadding(int padding) {
        mPadding = padding;
        requestLayout();
    }

    /**
     * Sets chars that are going to be displayed.
     * Note: <b>That only one digit is allow per character.</b>
     *
     * @param chars
     */
    public void setChars(char[] chars) {
        this.chars = chars;
    }

    public char[] getChars() {
        return chars;
    }


    public void setDividerColor(int color) {
        mDividerPaint.setColor(color);
        flush();
    }


    public void setTextColor(int color) {
        textPaint.setColor(color);
        flush();
    }

    public void setCornerSize(int cornerSize) {
        mCornerSize = cornerSize;
        flush();
    }

    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
        flush();
    }


//    public void play() {
//        tabAnimation.play();
//        flush();
//    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawColor(Color.BLUE);

        //tabs
        for (Page page : pages) {
            page.draw(canvas);
        }

        //divider
        canvas.save();
        canvas.concat(mProjectionMatrix);
        canvas.drawLine(-canvas.getWidth() / 2, 0, canvas.getWidth() / 2, 0, mDividerPaint);
        canvas.restore();
    }


    public void flush() {
        postInvalidate();
    }


    public class Page {

//        private final Matrix mModelViewMatrix = new Matrix();


        private final Matrix rotateMatrix = new Matrix();

//        private final RectF mStartBounds = new RectF();

//        private final RectF mEndBounds = new RectF();

        //当前显示字符的下标
        private int indexNow = 0;

        protected final static int LOWER_POSITION = 0;
        protected final static int MIDDLE_POSITION = 1;
        protected final static int UPPER_POSITION = 2;
        private int position;

//        private int mAlpha;

//        private Matrix mMeasuredMatrixHeight = new Matrix();
//
//        private Matrix mMeasuredMatrixWidth = new Matrix();


//        public void measure(int width, int height) {
//            Rect area = new Rect(-width / 2, 0, width / 2, height / 2);
//            mStartBounds.set(area);
//            mEndBounds.set(area);
//            mEndBounds.offset(0, -height / 2);
//        }
//
//        public int maxWidth() {
//            RectF rect = new RectF(mStartBounds);
//            Matrix projectionMatrix = new Matrix();
//            MatrixHelper.translate(projectionMatrix, mStartBounds.left, -mStartBounds.top, 0);
//            mMeasuredMatrixWidth.reset();
//            mMeasuredMatrixWidth.setConcat(projectionMatrix, MatrixHelper.ROTATE_X_90);
//            mMeasuredMatrixWidth.mapRect(rect);
//            return (int) rect.width();
//        }
//
//        public int maxHeight() {
//            RectF rect = new RectF(mStartBounds);
//            Matrix projectionMatrix = new Matrix();
//            mMeasuredMatrixHeight.reset();
//            mMeasuredMatrixHeight.setConcat(projectionMatrix, MatrixHelper.ROTATE_X_0);
//            mMeasuredMatrixHeight.mapRect(rect);
//            return (int) rect.height();
//        }

        public Page(int position) {
            this.position = position;
        }

        public void setChar(int index) {

            indexNow = index;
        }

        //显示下一个字符
        public void next() {
            indexNow++;
            if (indexNow >= chars.length) {
                indexNow = 0;
            }
        }

        int alpha;

        public void rotate(int alpha) {
            this.alpha = alpha;

        }

        public void draw(Canvas canvas) {


            float w = canvas.getWidth();
            float h = canvas.getHeight();
            if (w == 0 || h == 0) {
                return;
            }

            String textNow = Character.toString(chars[indexNow]);

            float[] size = TextMeasure.measure(textNow, textSize);

            float w_text = size[0];
            float h_text = size[1];

            float w_back_draw = 0;
            float h_back_draw = 0;

            float propertion = w / h;
            float propertion_text = w_text / h_text;

            if (propertion > propertion_text) {
                h_back_draw = h / 2;
                w_back_draw = h_back_draw * propertion_text;
            } else {
                w_back_draw = w;
                h_back_draw = w_back_draw / propertion_text / 2;
            }

            float y_center = h / 2;
            float x_center = w / 2;
            //绘制back
            canvas.save();
            if (position == MIDDLE_POSITION) {


//            mModelViewMatrix.set(rotateMatrix);
//            applyTransformation(canvas, mModelViewMatrix);

                camera.save();
                camera.rotateX(alpha);
                camera.getMatrix(rotateMatrix);
                camera.restore();

                /**
                 *      public static final int MSCALE_X = 0;   //!< use with getValues/setValues
                 *     public static final int MSKEW_X  = 1;   //!< use with getValues/setValues
                 *     public static final int MTRANS_X = 2;   //!< use with getValues/setValues
                 *     public static final int MSKEW_Y  = 3;   //!< use with getValues/setValues
                 *     public static final int MSCALE_Y = 4;   //!< use with getValues/setValues
                 *     public static final int MTRANS_Y = 5;   //!< use with getValues/setValues
                 *     public static final int MPERSP_0 = 6;   //!< use with getValues/setValues
                 *     public static final int MPERSP_1 = 7;   //!< use with getValues/setValues
                 *     public static final int MPERSP_2 = 8;   //!< use with getValues/setValues
                 */
                float scale = getContext().getResources().getDisplayMetrics().density;
                // 修正失真
                float[] mValues = new float[9];
                rotateMatrix.getValues(mValues);                //获取数值
//                S.s("scale:" + scale);
//                S.s("arr:        " + Arrays.toString(mValues));
//            mValues[6] = mValues[6] / scale;            //数值修正
//            mValues[7] = mValues[7] / scale;            //数值修正
//            mValues[8] = mValues[8] / scale;            //数值修正
//            mValues[8]*=2;
                rotateMatrix.setValues(mValues);                //重新赋值

                rotateMatrix.preTranslate(-x_center, -y_center);
                rotateMatrix.postTranslate(x_center, y_center);

                canvas.concat(rotateMatrix);
            }

            RectF rectF = new RectF();
            float left = (w - w_back_draw) / 2;
            float top = y_center - h_back_draw;
            float right = left + w_back_draw;
            float bottom = top + h_back_draw;

            switch (position) {
                case UPPER_POSITION:
                    textNow="上";
                    break;
                case MIDDLE_POSITION:
                    textNow="中";
                    break;
                case LOWER_POSITION:
                    textNow="下";
                    top = y_center;
                    bottom = top + h_back_draw;
                    break;
            }

            rectF.set(left, top, right, bottom);
            canvas.drawRoundRect(rectF, mCornerSize, mCornerSize, mBackgroundPaint);
            mBackgroundPaint.setStyle(Paint.Style.STROKE);
            mBackgroundPaint.setColor(Color.WHITE);
            mBackgroundPaint.setStrokeWidth(20);
            canvas.drawRoundRect(rectF, mCornerSize, mCornerSize, mBackgroundPaint);
            mBackgroundPaint.setStyle(Paint.Style.FILL);
            mBackgroundPaint.setColor(Color.BLACK);
            canvas.restore();

            //绘制数字
            canvas.save();

            RectF clip = rectF;
            if (position == MIDDLE_POSITION) {
                canvas.concat(rotateMatrix);
            }
            canvas.clipRect(clip);
            canvas.drawText(textNow, left, bottom, textPaint);
            canvas.restore();
        }

//        private void applyTransformation(Canvas canvas, Matrix matrix) {
//            mModelViewProjectionMatrix.reset();
//            mModelViewProjectionMatrix.setConcat(mProjectionMatrix, matrix);
//
//        }
    }

}
