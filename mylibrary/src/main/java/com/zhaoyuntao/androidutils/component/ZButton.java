package com.zhaoyuntao.androidutils.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.zhaoyuntao.androidutils.R;
import com.zhaoyuntao.androidutils.tools.*;
import com.zhaoyuntao.androidutils.tools.S;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by zhaoyuntao on 2017/11/13.
 */

public class ZButton extends FrameLayout {

    /**
     * false: click then be chosen, click again can not be unchosen, this will only be unchosen
     * by your code
     * true: click then be chosen,click again can be unchosen,
     */
    private boolean isAutoChange;
    private boolean isChoosen;
    private boolean isClick;

    private float percent_bitmap_center = 0.5f;
    private float percent_bitmap_left = 0.5f;
    private float percent_bitmap_right = 0.5f;
    private float radius;
    private float[] radiusArray = {0, 0, 0, 0};
    private float w_border;

    //center text
    private String text_center;
    private float textSize;
    private int textColor = Color.argb(90, 0, 0, 0);
    private int textColor_choose;
    private int textColor_click;
    private int textColor_disable;
    //center small text
    private String text_center_small;
    private float textSize_small;
    private int textColor_small;
    private int textColor_small_choose;
    private int textColor_small_click;
    private int textColor_small_disable;
    //left text
    private String text_left;
    private float textSize_left;
    private int textColor_left;
    private int textColor_left_choose;
    private int textColor_left_click;
    private int textColor_left_disable;
    //right text
    private String text_right;
    private float textSize_right;
    private int textColor_right;
    private int textColor_right_choose;
    private int textColor_right_click;
    private int textColor_right_disable;

    //space between bitmap and text
    private float w_space;
    //space between center text and small text
    private float w_space_text;

    private int color_text_border;
    private Bitmap drawable_center;
    private Bitmap drawable_left;
    private Bitmap drawable_right;
    private Bitmap drawable_center_choose;
    private Bitmap drawable_center_disable;
    private Bitmap drawable_center_click;
    private Bitmap drawable_back;
    private Bitmap drawable_back_choose;
    private Bitmap drawable_back_disable;
    private Bitmap drawable_back_click;
    /**
     * 中间图片和文字的排列方式
     */
    private String orientation;
    /**
     * 背景渐变方向, 默认是垂直渐变
     */
    private String orientation_shader = vertical;
    //background color
    private int color_back[] = new int[]{Color.WHITE};
    private int color_back_disable[] = new int[]{Color.LTGRAY};
    private int color_back_choose[] = new int[]{Color.LTGRAY};
    private int color_back_click[] = new int[]{Color.LTGRAY};
    private int color_border_choose;
    private int color_border_click;
    private int color_border;
    private int color_border_disable = Color.argb(55, 0, 0, 0);

    private int color_back_bitmap_center = Color.argb(0, 0, 0, 0);

    private boolean drawTextBorder = false;
    private boolean drawBitmapBorder_center;

    private int color_circle_border;
    private boolean drawCircleImg;
    public static final String vertical = "vertical";
    public static final String horizontal = "horizontal";

    //gravity of text and img
    private String gravity;
    public static final String gravity_img = "center_img";
    public static final String gravity_left = "left";
    public static final String gravity_both = "center_both";
    public static final String gravity_text = "center_text";
    public static final String gravity_interspace = "center_interspace";

    //clip of img
    private String style_bitmap_back = "center";
    //will not change the propertion of the img, it will clip the extra part to fill this canvas.
    public static final String style_bitmap_back_clip = "clip";//default
    //will change the propertion of the img to fill this canvas.
    public static final String style_bitmap_back_fill = "fill";
    //will not change the propertion of the img, but may not fill this canvas.
    public static final String style_bitmap_back_center = "center";

    //percent of length of the background color to draw.
    private float percent_back = 1;

    /**
     * some index values that use by yourself as you wish
     */
    private int index = 0;

    //width and height of view
    private int w, h;

    //互斥的button
    private Map<ZButton, ZButton> mapFriend;
    //是否显示progress
    private boolean isProgressType;

    public ZButton(Context context) {
        super(context);
        init(context, null);
    }

    public String getGravity() {
        return gravity;
    }

    public void setGravity(String gravity) {
        this.gravity = gravity;
        flush();
    }

    public ZButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ZButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isAutoChange() {
        return isAutoChange;
    }

    public void setAutoChange(boolean autoChange) {
        isAutoChange = autoChange;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        flush();
    }

    private void addFriend2(ZButton zButton) {
        if (zButton != null) {
            if (mapFriend == null) {
                mapFriend = new HashMap<>();
            }
            mapFriend.put(zButton, zButton);
            setAutoChange(true);
        }
    }

    public ZButton addFriend(ZButton zButton) {
        if (zButton != this) {
            if (mapFriend != null) {
                for (ZButton zButton1 : mapFriend.keySet()) {
                    zButton1.addFriend2(zButton);
                    zButton.addFriend2(zButton1);
                }
            }
            this.addFriend2(zButton);
            zButton.addFriend2(this);
        }
        return this;
    }

    private void removeFriend2(ZButton zButton) {
        if (mapFriend != null) {
            mapFriend.remove(zButton);
        }
    }

    /**
     * 参数中的这个button将会被互斥组内所有的button所抛弃
     *
     * @param zButton
     */
    public void removeFriend(ZButton zButton) {
        if (zButton != this) {
            if (mapFriend != null) {
                for (ZButton zButton1 : mapFriend.keySet()) {
                    zButton1.removeFriend2(zButton);
                    zButton.removeFriend2(zButton1);
                }
            }
            removeFriend2(zButton);
            if (zButton != null) {
                zButton.removeFriend2(this);
            }
        }
    }

    public void clearFriend() {
        if (mapFriend != null) {
            mapFriend.clear();
        }
    }

    ProgressBar progressBar;

    public void showProgress(boolean visibility) {
        if (progressBar != null) {
            progressBar.setVisibility(visibility ? VISIBLE : INVISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isClickable()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (this.isAutoChange) {
                        if (isChoosen()) {
                            //当没有互斥按钮时才可以自由改变其未被选中的状态
                            if (mapFriend == null || mapFriend.size() == 0) {
                                setChoosen(false);
                            } else {
                                //当存在互斥按钮时, 该按钮无法通过被点击改变其被选中的状态
                            }
                        } else {
                            setChoosen(true);
                        }
                    }
                    isClick = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    isClick = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    isClick = false;
                    break;
            }
            flush();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 可扩展的canvas
     *
     * @param canvas
     */
    protected void drawCanvas(Canvas canvas) {

    }

    ZImageView zImageView;

    /**
     * 在画布初始化之前进行一些操作,例如放置view
     */
    protected void underCanvas() {
    }

    /**
     * 在画布初始化之后
     */
    protected void afterAddCanvas() {
    }

    private void init(final Context context, AttributeSet attrs) {
        zImageView = new ZImageView(context);
        zImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        underCanvas();
        addView(zImageView);
        afterAddCanvas();
        if (attrs != null) {

            int color_default = Color.rgb(88, 88, 88);
            int textSize_default = B.sp2px(context, 20);

            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ZButton);//

            this.isProgressType = typedArray.getBoolean(R.styleable.ZButton_progress, false);

            this.wave = typedArray.getBoolean(R.styleable.ZButton_wave, false);
            this.waveColor = typedArray.getColor(R.styleable.ZButton_wavecolor, Color.WHITE);
            this.waveColor_choose = typedArray.getColor(R.styleable.ZButton_wavecolor_choose, waveColor);
            this.waveColor_click = typedArray.getColor(R.styleable.ZButton_wavecolor_click, waveColor);
            this.waveColor_disable = typedArray.getColor(R.styleable.ZButton_wavecolor_disable, waveColor);

            this.isAutoChange = typedArray.getBoolean(R.styleable.ZButton_isautochange, false);

            this.text_center = typedArray.getString(R.styleable.ZButton_text_center);//
            this.textSize = typedArray.getDimension(R.styleable.ZButton_textsize, textSize_default);//
            this.textColor = typedArray.getColor(R.styleable.ZButton_textcolor, textColor);//
            this.textColor_choose = typedArray.getColor(R.styleable.ZButton_textcolor_choose, textColor);//
            this.textColor_disable = typedArray.getColor(R.styleable.ZButton_textcolor_disable, textColor);//
            this.textColor_click = typedArray.getColor(R.styleable.ZButton_textcolor_click, textColor_choose);//

            this.text_center_small = typedArray.getString(R.styleable.ZButton_text_center_small);//
            this.textSize_small = typedArray.getDimension(R.styleable.ZButton_textsize_small, textSize_default);
            this.textColor_small = typedArray.getColor(R.styleable.ZButton_textcolor_small, color_default);//
            this.textColor_small_choose = typedArray.getColor(R.styleable.ZButton_textcolor_small_choose, color_default);//
            this.textColor_small_click = typedArray.getColor(R.styleable.ZButton_textcolor_small_click, color_default);//
            this.textColor_small_disable = typedArray.getColor(R.styleable.ZButton_textcolor_small_disable, color_default);//

            this.text_left = typedArray.getString(R.styleable.ZButton_text_left);//
            this.textSize_left = typedArray.getDimension(R.styleable.ZButton_textsize_left, textSize_default);
            this.textColor_left = typedArray.getColor(R.styleable.ZButton_textcolor_left, color_default);//
            this.textColor_left_choose = typedArray.getColor(R.styleable.ZButton_textcolor_left_choose, color_default);//
            this.textColor_left_click = typedArray.getColor(R.styleable.ZButton_textcolor_left_click, color_default);//
            this.textColor_left_disable = typedArray.getColor(R.styleable.ZButton_textcolor_left_disable, color_default);//

            this.text_right = typedArray.getString(R.styleable.ZButton_text_right);//
            this.textSize_right = typedArray.getDimension(R.styleable.ZButton_textsize_right, textSize_default);
            this.textColor_right = typedArray.getColor(R.styleable.ZButton_textcolor_right, color_default);//
            this.textColor_right_choose = typedArray.getColor(R.styleable.ZButton_textcolor_right_choose, color_default);//
            this.textColor_right_click = typedArray.getColor(R.styleable.ZButton_textcolor_right_click, color_default);//
            this.textColor_right_disable = typedArray.getColor(R.styleable.ZButton_textcolor_right_click, color_default);//

            //渐变
            this.color_back = getColorsFromAttr(typedArray.getString(R.styleable.ZButton_color_back));
            if (color_back == null || color_back.length == 0) {
                color_back = new int[]{Color.WHITE};
            }
            this.color_back_choose = getColorsFromAttr(typedArray.getString(R.styleable.ZButton_color_back_choose));
            if (color_back_choose == null || color_back_choose.length == 0) {
                color_back_choose = new int[]{Color.LTGRAY};
            }
            this.color_back_disable = getColorsFromAttr(typedArray.getString(R.styleable.ZButton_color_back_disable));
            if (color_back_disable == null || color_back_disable.length == 0) {
                color_back_disable = new int[]{Color.LTGRAY};
            }
            this.color_back_click = getColorsFromAttr(typedArray.getString(R.styleable.ZButton_color_back_click));
            if (color_back_click == null || color_back_click.length == 0) {
                color_back_click = new int[]{Color.LTGRAY};
            }

            this.orientation_shader = typedArray.getString(R.styleable.ZButton_orientation_shader);
            if (S.isEmpty(orientation_shader)) {
                orientation_shader = vertical;
            }

            this.color_back_bitmap_center = typedArray.getColor(R.styleable.ZButton_color_border_bitmap_center, color_back[0]);//

            this.color_border = typedArray.getColor(R.styleable.ZButton_color_border, color_border);//
            this.color_border_choose = typedArray.getColor(R.styleable.ZButton_color_border_choose, color_border);//
            this.color_border_click = typedArray.getColor(R.styleable.ZButton_color_border_click, color_border_choose);//

            this.drawable_center = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_center));
            this.drawable_left = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_left));
            this.drawable_right = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_right));
            this.drawable_center_choose = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_center_choose));
            if (drawable_center_choose == null) {
                drawable_center_choose = drawable_center;
            }
            this.drawable_center_disable = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_center_disable));
            if (drawable_center_disable == null) {
                drawable_center_disable = drawable_center;
            }
            this.drawable_center_click = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_center_click));
            if (drawable_center_click == null) {
                drawable_center_click = drawable_center_choose;
            }
            this.drawable_back = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_back));
            this.drawable_back_choose = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_back_choose));

            this.drawable_back_disable = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_back_disable));

            this.drawable_back_click = B.drawableToBitmap(typedArray.getDrawable(R.styleable.ZButton_img_back_click));
            if (drawable_back_choose == null) {
                drawable_back_choose = drawable_back;
            }
            if (drawable_back_disable == null) {
                drawable_back_disable = drawable_back;
            }
            if (drawable_back_click == null) {
                drawable_back_click = drawable_back_choose;
            }
            this.percent_back = typedArray.getFloat(R.styleable.ZButton_percent_back, 1f);
            if (percent_back < 0) {
                percent_back = 0;
            } else if (percent_back > 1) {
                percent_back = 1;
            }

            this.percent_bitmap_center = typedArray.getFloat(R.styleable.ZButton_percent_bitmap_center, 1f);
            this.percent_bitmap_left = typedArray.getFloat(R.styleable.ZButton_percent_bitmap_left, 1f);
            this.percent_bitmap_right = typedArray.getFloat(R.styleable.ZButton_percent_bitmap_right, 1f);
            this.w_space = typedArray.getDimension(R.styleable.ZButton_w_space, 0);
            this.w_space_text = typedArray.getDimension(R.styleable.ZButton_w_space_text, 0);
            this.w_border = typedArray.getDimension(R.styleable.ZButton_w_border, 0);
            this.drawTextBorder = typedArray.getBoolean(R.styleable.ZButton_drawtextborder, false);
            this.color_text_border = typedArray.getColor(R.styleable.ZButton_color_text_border, Color.BLACK);//
            this.drawBitmapBorder_center = typedArray.getBoolean(R.styleable.ZButton_drawimgborder_center, false);
            this.color_circle_border = typedArray.getColor(R.styleable.ZButton_color_circle_border, Color.WHITE);//
            this.drawCircleImg = typedArray.getBoolean(R.styleable.ZButton_drawcircleimg, false);

            this.radius = typedArray.getDimension(R.styleable.ZButton_radius, 0);
            if (radius < 0) {
                radius = 0;
            }
            this.radiusArray[0] = typedArray.getDimension(R.styleable.ZButton_radius_lefttop, radius);
            this.radiusArray[1] = typedArray.getDimension(R.styleable.ZButton_radius_righttop, radius);
            this.radiusArray[2] = typedArray.getDimension(R.styleable.ZButton_radius_rightbottom, radius);
            this.radiusArray[3] = typedArray.getDimension(R.styleable.ZButton_radius_leftbottom, radius);

            this.style_bitmap_back = typedArray.getString(R.styleable.ZButton_style_img_back);

            if (this.style_bitmap_back == null || this.style_bitmap_back.equals("")) {
                this.style_bitmap_back = style_bitmap_back_clip;
            }

            this.gravity = typedArray.getString(R.styleable.ZButton_gravity_img_center);
            if (gravity == null) {
                gravity = gravity_both;
            }
            this.orientation = typedArray.getString(R.styleable.ZButton_orientation);
            if (orientation == null) {
                orientation = vertical;
            }
            typedArray.recycle();
        }
        if (this.isProgressType) {
            this.color_back = new int[]{Color.argb(0, 0, 0, 0)};
            this.color_back_disable = new int[]{Color.argb(0, 0, 0, 0)};
            this.color_back_choose = new int[]{Color.argb(0, 0, 0, 0)};
            this.color_back_click = new int[]{Color.argb(0, 0, 0, 0)};
        }

        //进度
        progressBar = new ProgressBar(getContext());
        ZButton.LayoutParams lp = new ZButton.LayoutParams(B.dip2px(getContext(), 50), B.dip2px(getContext(), 50));
        lp.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(lp);
        addView(progressBar);
        showProgress(false);
    }

    private int[] getColorsFromAttr(String colorStr) {
        if (S.isEmpty(colorStr)) {
            return null;
        }
        String[] colorStrArr = colorStr.split("#");
        if (colorStrArr == null) {
            return null;
        }
        int[] arr_color = new int[0];
        for (int i = 0; i < colorStrArr.length; i++) {
            if (S.isEmpty(colorStrArr[i])) {
                continue;
            }
            String colorTmpStr = "#" + colorStrArr[i].trim();
            int color = B.hexColorToIntColor(colorTmpStr);
            arr_color = Arrays.copyOf(arr_color, arr_color.length + 1);
            arr_color[arr_color.length - 1] = color;
        }
        return arr_color;
    }

    public float getPercent_back() {
        return percent_back;
    }

    public void setPercent_back(float percent_back) {

        if (percent_back < 0) {
            percent_back = 0;
        } else if (percent_back > 1) {
            percent_back = 1;
        }
        this.percent_back = percent_back;
        flush();
    }

    public String getStyle_bitmap_back() {
        return style_bitmap_back;
    }

    public void setStyle_bitmap_back(String style_bitmap_back) {
        this.style_bitmap_back = style_bitmap_back;
        flush();
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        this.isClick = click;
        flush();
    }

    public int getTextColor_click() {
        return textColor_click;
    }

    public void setTextColor_click(int textColor_click) {
        this.textColor_click = textColor_click;
        flush();
    }

    public Bitmap getDrawable_center_click() {
        return drawable_center_click;
    }

    public void setDrawable_center_click(Bitmap drawable_center_click) {
        if (drawable_center_click == null) {
            this.drawable_center_click = drawable_center_choose;
        } else {
            this.drawable_center_click = drawable_center_click;
        }
        flush();
    }

    public Bitmap getDrawable_back_click() {
        return drawable_back_click;
    }

    public void setDrawable_back_click(Bitmap drawable_back_click) {
        this.drawable_back_click = drawable_back_click;
        flush();
    }


    public int getColor_border_click() {
        return color_border_click;
    }

    public void setColor_border_click(int color_border_click) {
        this.color_border_click = color_border_click;
        flush();
    }

    private void setSelfChoosen(boolean isChoosen) {
        this.isChoosen = isChoosen;
        flush();
    }

    public void setChoosen(boolean isChoosen) {
        setSelfChoosen(isChoosen);
        if (mapFriend != null && isChoosen) {
            for (ZButton zButton : mapFriend.keySet()) {
                if (zButton != null) {
                    zButton.setSelfChoosen(false);
                }
            }
        }
    }

    @Override
    public void postInvalidate() {
        super.postInvalidate();
        if (zImageView != null) {
            zImageView.postInvalidate();
        }
    }


    public String getText_left() {
        return text_left;
    }

    public void setText_left(String text_left) {
        this.text_left = text_left;
        flush();
    }

    public String getText_right() {
        return text_right;
    }

    public void setText_right(String text_right) {
        this.text_right = text_right;
        flush();
    }

    public float getPercent_bitmap_left() {
        return percent_bitmap_left;
    }

    public void setPercent_bitmap_left(float percent_bitmap_left) {
        this.percent_bitmap_left = percent_bitmap_left;
        flush();
    }

    public float getPercent_bitmap_right() {
        return percent_bitmap_right;
    }

    public void setPercent_bitmap_right(float percent_bitmap_right) {
        this.percent_bitmap_right = percent_bitmap_right;
        flush();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        flush();
    }

    public int getColor_text_border() {
        return color_text_border;
    }

    public void setColor_text_border(int color_text_border) {
        this.color_text_border = color_text_border;
        flush();
    }

    public Bitmap getDrawable_center() {
        return drawable_center;
    }

    public Bitmap getDrawable_left() {
        return drawable_left;
    }

    public void setDrawable_left(Bitmap drawable_left) {
        this.drawable_left = drawable_left;
        flush();
    }

    public Bitmap getDrawable_right() {
        return drawable_right;
    }

    public void setDrawable_right(Bitmap drawable_right) {
        this.drawable_right = drawable_right;
        flush();
    }

    public Bitmap getDrawable_back() {
        return drawable_back;
    }

    public float getW_space_text() {
        return w_space_text;
    }

    public void setW_space_text(float w_space_text) {
        this.w_space_text = w_space_text;
        flush();
    }


    public boolean isDrawTextBorder() {
        return drawTextBorder;
    }

    public void setDrawTextBorder(boolean drawTextBorder) {
        this.drawTextBorder = drawTextBorder;
        flush();
    }

    public boolean isDrawBitmapBorder_center() {
        return drawBitmapBorder_center;
    }

    public void setDrawBitmapBorder_center(boolean drawBitmapBorder_center) {
        this.drawBitmapBorder_center = drawBitmapBorder_center;
        flush();
    }


    public int getColor_circle_border() {
        return color_circle_border;
    }

    public void setColor_circle_border(int color_circle_border) {
        this.color_circle_border = color_circle_border;
        flush();
    }

    public boolean isDrawCircleImg() {
        return drawCircleImg;
    }

    public void setDrawCircleImg(boolean drawCircleImg) {
        this.drawCircleImg = drawCircleImg;
        flush();
    }


    public float[] getRadiusArray() {
        return radiusArray;
    }

    public void setRadiusArray(float[] radiusArray) {
        this.radiusArray = radiusArray;
        flush();
    }

    public String getText_center() {
        return text_center;
    }

    public void setText_center(String text_center) {
        this.text_center = text_center;
        flush();
    }

    public String getText_center_small() {
        return text_center_small;
    }

    public void setText_center_small(String text_center_small) {
        this.text_center_small = text_center_small;
        flush();
    }

    public boolean isChoosen() {
        return isChoosen;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {

        this.textSize = B.sp2px(getContext(), textSize);
        flush();
    }

    public float getPercent_bitmap_center() {
        return percent_bitmap_center;
    }

    public void setPercent_bitmap_center(float percent_bitmap_center) {
        this.percent_bitmap_center = percent_bitmap_center;
        flush();
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        flush();
    }

    public float getW_border() {
        return w_border;
    }

    public void setW_border(float w_border) {
        this.w_border = w_border;
        flush();
    }

    public int getTextColor_choose() {
        return textColor_choose;
    }

    public void setTextColor_choose(int textColor_choose) {
        this.textColor_choose = textColor_choose;
        flush();
    }


    public Bitmap getDrawable_center_choose() {
        return drawable_center_choose;
    }

    public void setDrawable_center_choose(Bitmap drawable_center_choose) {
        if (drawable_center_choose == null) {
            this.drawable_center_choose = drawable_center;
        } else {
            this.drawable_center_choose = drawable_center_choose;
        }
        flush();
    }


    public Bitmap getDrawable_back_choose() {
        return drawable_back_choose;
    }

    public void setDrawable_back_choose(Bitmap drawable_back_choose) {
        this.drawable_back_choose = drawable_back_choose;
        flush();
    }


    public void setDrawable_back(Bitmap drawable_back) {
        this.drawable_back = drawable_back;
        if (this.drawable_back_click == null) {
            this.drawable_back_click = drawable_back;
        }
        flush();
    }

    public float getW_space() {
        return w_space;
    }

    public void setW_space(float w_space) {
        this.w_space = w_space;
        flush();
    }

    public static String getOrientation_horizontal() {
        return horizontal;
    }

    public static String getOrientation_vertical() {
        return vertical;
    }


    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
        flush();
    }


    public void setColor_back(int... color_back) {
        if (color_back != null && color_back.length > 0) {
            this.color_back = color_back;
            flush();
        }
    }


    public void setColor_back_choose(int... color_back_choose) {
        if (color_back_choose != null && color_back_choose.length > 0) {
            this.color_back_choose = color_back_choose;
            flush();
        }
    }

    public void setColor_back_disable(int... color_back_disable) {
        if (color_back_disable != null && color_back_disable.length > 0) {
            this.color_back_disable = color_back_disable;
            flush();
        }
    }

    public void setColor_back_click(int... color_back_click) {
        if (color_back_click != null && color_back_click.length > 0) {
            this.color_back_click = color_back_click;
            flush();
        }
    }

    public int getColor_border_choose() {
        return color_border_choose;
    }

    public void setColor_border_choose(int color_border_choose) {
        this.color_border_choose = color_border_choose;
        flush();
    }

    public int getColor_border() {
        return color_border;
    }

    public void setColor_border(int color_border_unchoose) {
        this.color_border = color_border;
        flush();
    }

    public void setDrawable_center(Bitmap drawable_center) {
        this.drawable_center = drawable_center;
        this.drawable_center_choose = drawable_center;
        this.drawable_center_click = drawable_center;
        flush();
    }

    public void clearCenter() {
        this.drawable_center = null;
        this.drawable_center_choose = null;
        this.drawable_center_click = null;
        this.text_center = null;
        this.text_center_small = null;
        flush();
    }

    private int w_wave;
    private int h_wave;
    private int x_wave;
    private int y_wave;
    private boolean wave;
    private int waveColor;
    private int waveColor_click;
    private int waveColor_choose;
    private int waveColor_disable;
    ValueAnimator waveAnimator;

    public void setWave(boolean wave) {
        this.wave = wave;
        if (wave) {
            startWaveAnimation();
        } else {
            stopAnimation();
        }
        flush();
    }

    private void stopAnimation() {
        if (waveAnimator != null && waveAnimator.isRunning()) {
            waveAnimator.cancel();
            waveAnimator = null;
        }
    }

    public void startWaveAnimation() {
        stopAnimation();
        waveAnimator = ValueAnimator.ofInt(0, w_wave);
        waveAnimator.setDuration(2000);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                x_wave = (int) valueAnimator.getAnimatedValue();
                if (x_wave == w_wave) {
                    x_wave = 0;
                }
                y_wave = h / 4;
                flush();
            }
        });
        waveAnimator.start();
    }

    public void flush() {
        postInvalidate();
    }

    public ImageView getImageView() {
        return zImageView;
    }

    private int w_bitmap_back_draw;
    private int h_bitmap_back_draw;
    private int x_bitmap_back_draw;
    private int y_bitmap_back_draw;

    public int getW_bitmap_back_draw() {
        return w_bitmap_back_draw;
    }

    public int getH_bitmap_back_draw() {
        return h_bitmap_back_draw;
    }

    public int getX_bitmap_back_draw() {
        return x_bitmap_back_draw;
    }

    public int getY_bitmap_back_draw() {
        return y_bitmap_back_draw;
    }

    class ZImageView extends androidx.appcompat.widget.AppCompatImageView {

        public ZImageView(Context context) {
            super(context);
        }

        public ZImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ZImageView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            w = getWidth();

            h = getHeight();

            if (w == 0 || h == 0) {
                return;
            }

            float w_half = w / 2f;
            float h_half = h / 2f;

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Paint paint_back = new Paint();

            //draw background color
            int w_rect = (int) (w * percent_back);
            int h_rect = h;
            int w_rect_radius_lefttop = (int) (radiusArray[0] * 2);
            int w_rect_radius_righttop = (int) (radiusArray[1] * 2);
            int w_rect_radius_rightbottom = (int) (radiusArray[2] * 2);
            int w_rect_radius_leftbottom = (int) (radiusArray[3] * 2);
            Path path_back = new Path();
            path_back.moveTo(0, radiusArray[0]);//0
            path_back.arcTo(new RectF(0, 0, w_rect_radius_lefttop, w_rect_radius_lefttop), 180, 90);//1
            path_back.lineTo(w_rect - radiusArray[1], 0);//2
            path_back.arcTo(new RectF(w_rect - w_rect_radius_righttop, 0, w_rect, w_rect_radius_righttop), 270, 90);
            path_back.lineTo(w_rect, h_rect - radiusArray[2]);//4
            path_back.arcTo(new RectF(w_rect - w_rect_radius_rightbottom, h_rect - w_rect_radius_rightbottom, w_rect, h_rect), 0, 90);
            path_back.lineTo(radiusArray[3], h_rect);//6
            path_back.arcTo(new RectF(0, h_rect - w_rect_radius_leftbottom, w_rect_radius_leftbottom, h_rect), 90, 90);
            path_back.lineTo(0, radiusArray[3]);

            canvas.clipPath(path_back);


            Paint paint_border = new Paint();

            float radius_min = (w > h ? h : w) / 2f;
            if (radius > radius_min) {
                radius = radius_min;
            }

            Bitmap drawable_back_draw;
            Bitmap drawable_center_draw;


            int textColor_tmp;
            int textColor_small_tmp;
            int textColor_left_tmp;
            int textColor_right_tmp;
            int borderColor_tmp;
            int waveColor_tmp;

            //if disabled , all the active will be forbid .
            if (ZButton.this.isEnabled()) {
                //click is first level
                if (isClick) {
                    drawable_back_draw = drawable_back_click;
                    drawable_center_draw = drawable_center_click;
                    if (color_back_click != null) {
                        if (color_back_click.length > 1) {
                            int x_start = 0;
                            int y_start = 0;
                            int x_end = 0;
                            int y_end = 0;
                            if (S.isNotEmpty(orientation_shader)) {
                                if (vertical.equals(orientation_shader)) {
                                    y_end = h_rect;
                                } else if (horizontal.equals(orientation_shader)) {
                                    x_end = w_rect;
                                }
                            }
                            Shader shader = new LinearGradient(x_start, y_start, x_end, y_end, color_back_click, null, Shader.TileMode.REPEAT);
                            paint_back.setShader(shader);
                        } else if (color_back_click.length == 1) {
                            paint_back.setColor(color_back_click[0]);
                        }
                    }
                    textColor_tmp = ZButton.this.textColor_click;
                    textColor_small_tmp = textColor_small_click;
                    textColor_left_tmp = textColor_left_click;
                    textColor_right_tmp = textColor_right_click;
                    borderColor_tmp = color_border_click;
                    waveColor_tmp = waveColor_click;

                } else if (isChoosen) {
                    drawable_back_draw = drawable_back_choose;
                    drawable_center_draw = drawable_center_choose;
                    textColor_tmp = ZButton.this.textColor_choose;
                    borderColor_tmp = color_border_choose;
                    textColor_small_tmp = textColor_small_choose;
                    textColor_left_tmp = textColor_left_choose;
                    textColor_right_tmp = textColor_right_choose;
                    if (color_back_choose != null) {
                        if (color_back_choose.length > 1) {
                            int x_start = 0;
                            int y_start = 0;
                            int x_end = 0;
                            int y_end = 0;
                            if (S.isNotEmpty(orientation_shader)) {
                                if (vertical.equals(orientation_shader)) {
                                    y_end = h_rect;
                                } else if (horizontal.equals(orientation_shader)) {
                                    x_end = w_rect;
                                }
                            }
                            Shader shader = new LinearGradient(x_start, y_start, x_end, y_end, color_back_choose, null, Shader.TileMode.REPEAT);
                            paint_back.setShader(shader);
                        } else if (color_back_choose.length == 1) {
                            paint_back.setColor(color_back_choose[0]);
                        }
                    }
                    waveColor_tmp = waveColor_choose;
                } else {
                    drawable_back_draw = drawable_back;
                    drawable_center_draw = drawable_center;
                    textColor_tmp = ZButton.this.textColor;
                    borderColor_tmp = color_border;
                    textColor_small_tmp = textColor_small;
                    textColor_left_tmp = textColor_left;
                    textColor_right_tmp = textColor_right;
                    if (color_back != null) {
                        if (color_back.length > 1) {
                            int x_start = 0;
                            int y_start = 0;
                            int x_end = 0;
                            int y_end = 0;
                            if (S.isNotEmpty(orientation_shader)) {
                                if (vertical.equals(orientation_shader)) {
                                    y_end = h_rect;
                                } else if (horizontal.equals(orientation_shader)) {
                                    x_end = w_rect;
                                }
                            }
                            Shader shader = new LinearGradient(x_start, y_start, x_end, y_end, color_back, null, Shader.TileMode.REPEAT);
                            paint_back.setShader(shader);
                        } else if (color_back.length == 1) {
                            paint_back.setColor(color_back[0]);
                        }
                    }
                    waveColor_tmp = waveColor;
                }
            } else {
                drawable_back_draw = drawable_back_disable;
                drawable_center_draw = drawable_center_disable;
                if (color_back_disable != null) {
                    if (color_back_disable.length > 1) {
                        int x_start = 0;
                        int y_start = 0;
                        int x_end = 0;
                        int y_end = 0;
                        if (S.isNotEmpty(orientation_shader)) {
                            if (vertical.equals(orientation_shader)) {
                                y_end = h_rect;
                            } else if (horizontal.equals(orientation_shader)) {
                                x_end = w_rect;
                            }
                        }
                        Shader shader = new LinearGradient(x_start, y_start, x_end, y_end, color_back_disable, null, Shader.TileMode.REPEAT);
                        paint_back.setShader(shader);
                    } else if (color_back_disable.length == 1) {
                        paint_back.setColor(color_back_disable[0]);
                    }
                }
                textColor_tmp = ZButton.this.textColor_disable;
                textColor_small_tmp = textColor_small_disable;
                textColor_left_tmp = textColor_left_disable;
                textColor_right_tmp = textColor_right_disable;
                borderColor_tmp = color_border_disable;
                waveColor_tmp = waveColor_disable;
            }
            paint_border.setColor(borderColor_tmp);

            paint_back.setStyle(Paint.Style.FILL);
            paint_back.setAntiAlias(true);


            canvas.drawPath(path_back, paint_back);


            if (wave) {
                if (w_wave == 0) {
                    w_wave = w;
                }

                if (h_wave == 0) {
                    h_wave = 30;
                }

                if (waveAnimator == null) {
                    startWaveAnimation();
                }


                Path path_wave = new Path();
                path_wave.moveTo(-w_wave + x_wave, y_wave);
                for (int i = -w_wave; i < getWidth() + w_wave; i += w_wave) {
                    path_wave.rQuadTo(w_wave / 4, -h_wave, w_wave / 2, 0);
                    path_wave.rQuadTo(w_wave / 4, h_wave, w_wave / 2, 0);
                }
                path_wave.lineTo(w, h);
                path_wave.lineTo(0, getHeight());
                path_wave.close();
                Paint paint_wave = new Paint();
                paint_wave.setColor(waveColor_tmp);
                paint_wave.setAntiAlias(true);
                paint_wave.setStyle(Paint.Style.FILL);
                canvas.save();
                canvas.clipPath(path_back);
                canvas.drawPath(path_wave, paint_wave);
                canvas.restore();
            }

            //draw background img
            if (drawable_back_draw != null) {
                w_bitmap_back_draw = w;
                h_bitmap_back_draw = h;

                //get size of the img
                int w_bitmap_back = drawable_back_draw.getWidth();
                int h_bitmap_back = drawable_back_draw.getHeight();


                float propprtion_bitmap = (float) w_bitmap_back / h_bitmap_back;
                float propprtion_view = (float) w / h;

                switch (style_bitmap_back) {
                    case style_bitmap_back_center:
                        if (propprtion_bitmap > propprtion_view) {
                            w_bitmap_back_draw = w;
                            h_bitmap_back_draw = (int) (w_bitmap_back_draw / propprtion_bitmap);
                        } else {
                            h_bitmap_back_draw = h;
                            w_bitmap_back_draw = (int) (h_bitmap_back_draw * propprtion_bitmap);
                        }
                        break;
                    case style_bitmap_back_fill:
                        w_bitmap_back_draw = w;
                        h_bitmap_back_draw = h;
                        break;
                    case style_bitmap_back_clip:
                    default:
                        if (propprtion_bitmap > propprtion_view) {
                            h_bitmap_back_draw = h;
                            w_bitmap_back_draw = (int) (h_bitmap_back_draw * propprtion_bitmap);
                        } else {
                            w_bitmap_back_draw = w;
                            h_bitmap_back_draw = (int) (w_bitmap_back_draw / propprtion_bitmap);
                        }
                }
                x_bitmap_back_draw = (w - w_bitmap_back_draw) / 2;
                y_bitmap_back_draw = (h - h_bitmap_back_draw) / 2;

                //scale img
                Rect rect_src_bitmap = new Rect();
                rect_src_bitmap.set(0, 0, w_bitmap_back, h_bitmap_back);
                Rect rect_des_bitmap = new Rect();
                rect_des_bitmap.set(0, 0, w_bitmap_back_draw, h_bitmap_back_draw);
                drawable_back_draw = B.getBitmap_rect(drawable_back_draw, rect_src_bitmap, rect_des_bitmap);
                drawable_back_draw = B.getBitmap_polygon(drawable_back_draw, path_back, x_bitmap_back_draw, y_bitmap_back_draw);
                //calculate size of img scaled
                w_bitmap_back = drawable_back_draw.getWidth();
                h_bitmap_back = drawable_back_draw.getHeight();
                Rect rect_src_back = new Rect();
                rect_src_back.set(0, 0, w_bitmap_back, h_bitmap_back);
                RectF rect_destination_back = new RectF();
                rect_destination_back.set(x_bitmap_back_draw, y_bitmap_back_draw, x_bitmap_back_draw + w_bitmap_back_draw, y_bitmap_back_draw + h_bitmap_back_draw);

                canvas.drawBitmap(drawable_back_draw, rect_src_back, rect_destination_back, paint);
            }

            //icon will be drawn on left side and right side of this canvas
            Bitmap drawable_left_draw = ZButton.this.drawable_left;
            Bitmap drawable_right_draw = ZButton.this.drawable_right;

            float x_bitmap_center_draw = 0;
            float y_bitmap_center_draw = 0;
            float x_text_center_draw = 0;
            float y_text_center_draw = 0;
            float w_text_center = 0;
            float h_text_center = 0;
            float x_text_center_small_draw = 0;
            float y_text_center_small_draw = 0;
            float w_text_center_small = 0;
            float h_text_center_small = 0;
            float w_arr_center = 0;
            float h_text_arr = 0;
            int w_bitmap_center = 0;
            int h_bitmap_center = 0;
            float h_bitmap_center_draw = 0;
            float w_bitmap_center_draw = 0;

            float x_bitmap_left_draw = 0;
            float y_bitmap_left_draw = 0;
            float x_text_left_draw = 0;
            float y_text_left_draw = 0;
            float w_text_left = 0;
            float h_text_left = 0;
            float w_bitmap_left_draw = 0;
            float h_bitmap_left_draw = 0;
            float w_bitmap_left_hold = 0;


            float x_bitmap_right_draw = 0;
            float y_bitmap_right_draw = 0;
            float x_text_right_draw = 0;
            float y_text_right_draw = 0;
            float w_text_right = 0;
            float h_text_right = 0;
            float h_bitmap_right_draw = 0;
            float w_bitmap_right_draw = 0;
            float w_bitmap_right_hold = 0;

            float h_item_textarr = 0;

            //calculate size of text
            if (com.zhaoyuntao.androidutils.tools.S.isNotEmpty(text_center)) {
                float[] size_text_center = TextMeasure.measure(text_center, textSize);
                w_text_center = size_text_center[0];
                h_text_center = size_text_center[1];
            }
            if (text_center_small != null) {
                float[] arr = TextMeasure.measure(text_center_small, textSize_small);
                w_text_center_small = arr[0];
                h_text_center_small = arr[1];
            }
            if (text_left != null) {
                float[] arr = TextMeasure.measure(text_left, textSize_left);
                w_text_left = arr[0];
                h_text_left = arr[1];
            }
            if (text_right != null) {
                float[] arr = TextMeasure.measure(text_right, textSize_right);
                w_text_right = arr[0];
                h_text_right = arr[1];
            }

            //calculate img w and h of center
            if (drawable_center_draw != null) {
                //draw circle img
                if (drawCircleImg) {
                    drawable_center_draw = B.getBitmap_circle(drawable_center_draw);
                }
                w_bitmap_center = drawable_center_draw.getWidth();
                h_bitmap_center = drawable_center_draw.getHeight();
                if (percent_bitmap_center == 0) {
                    percent_bitmap_center = 1f;
                }
                h_bitmap_center_draw = h * percent_bitmap_center * 0.5f;
                w_bitmap_center_draw = h_bitmap_center_draw * (((float) w_bitmap_center) / h_bitmap_center);
            }

            //calculate img x and y of center
            switch (gravity) {
                case gravity_interspace://重叠在一起
                case gravity_img://以图片为中心
                    x_bitmap_center_draw = w_half - w_bitmap_center_draw / 2f;
                    y_bitmap_center_draw = h_half - h_bitmap_center_draw / 2f;
                    break;
                case gravity_text://以文字为中心
                    x_bitmap_center_draw = w_half - (w_bitmap_center_draw + w_space + w_text_center / 2f);
                    y_bitmap_center_draw = h_half - (h_bitmap_center_draw + w_space + h_text_center / 2f);
                    break;
                case gravity_left://整体靠左
                    switch (orientation) {
                        case vertical:
                            x_text_center_draw = 0;
                            y_bitmap_center_draw = h_half - (h_bitmap_center_draw + w_space + h_text_center + w_space_text + h_text_center_small) / 2f;
                            break;
                        case horizontal:
                            x_text_center_draw = 0;
                            y_bitmap_center_draw = h_half - h_bitmap_center_draw / 2f;
                            break;
                    }
                    break;
                default:
                    switch (orientation) {
                        case vertical:
                            x_bitmap_center_draw = w_half - w_bitmap_center_draw / 2f;
                            y_bitmap_center_draw = h_half - (h_bitmap_center_draw + w_space + h_text_center + w_space_text + h_text_center_small) / 2f;
                            break;
                        case horizontal:
                            x_bitmap_center_draw = w_half - (w_bitmap_center_draw + w_space + w_text_center) / 2f;
                            y_bitmap_center_draw = h_half - h_bitmap_center_draw / 2f;
                            break;
                    }
                    break;
            }

            switch (gravity) {
                case gravity_interspace://重叠在一起
                case gravity_text://以文字为中心
                    x_text_center_draw = w_half - w_text_center / 2f;
                    y_text_center_draw = h_half - (h_text_center + w_space_text + h_text_center_small) / 2f + h_text_center;
                    break;
                case gravity_img://以图片为中心
                    switch (orientation) {
                        case vertical:
                            x_text_center_draw = w_half - w_text_center / 2f;
                            y_text_center_draw = y_bitmap_center_draw + w_space;
                            break;
                        case horizontal:
                            x_text_center_draw = x_bitmap_center_draw + w_bitmap_center_draw + w_space;
                            y_text_center_draw = h_half - (h_text_center + w_space_text + h_text_center_small) / 2f + h_text_center;
                            break;
                    }
                    break;
                case gravity_left://整体靠左
                    switch (orientation) {
                        case vertical:
                            x_text_center_draw = 0;
                            y_text_center_draw = y_bitmap_center_draw + h_bitmap_center_draw + w_space + h_text_center;
                            break;
                        case horizontal:
                            x_text_center_draw = 0;
                            y_text_center_draw = h_half - (h_text_center + w_space_text + h_text_center_small) / 2f + h_text_center;
                            break;
                    }
                    break;
                default://以二者中心为中心
                    switch (orientation) {
                        case vertical:
                            x_text_center_draw = w_half - w_text_center / 2f;
                            y_text_center_draw = y_bitmap_center_draw + h_bitmap_center_draw + w_space + h_text_center;
                            break;
                        case horizontal:
                            x_text_center_draw = x_bitmap_center_draw + w_bitmap_center_draw + w_space;
                            y_text_center_draw = h_half - (h_text_center + w_space_text + h_text_center_small) / 2f + h_text_center;
                            break;
                    }
                    break;
            }

            //根据中间文字坐标来计算小字的坐标
            x_text_center_small_draw = x_text_center_draw;
            y_text_center_small_draw = y_text_center_draw + w_space_text + h_text_center_small;

            if (drawable_center_draw != null) {

                //draw bitmap
                Rect rect_src = new Rect();
                rect_src.set(0, 0, w_bitmap_center, h_bitmap_center);
                Rect rect_des = new Rect();
                rect_des.set((int) x_bitmap_center_draw, (int) y_bitmap_center_draw, (int) (x_bitmap_center_draw + w_bitmap_center_draw), (int) (y_bitmap_center_draw + h_bitmap_center_draw));
                canvas.drawBitmap(drawable_center_draw, rect_src, rect_des, paint);

                if (drawBitmapBorder_center) {

                    if (drawCircleImg) {
                        float w_stroke = w_border;
                        paint.setColor(color_circle_border);
                        paint.setAntiAlias(true);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(w_stroke);
                        float radius_circle = w_bitmap_center_draw / 2;
                        float x_circle = x_bitmap_center_draw + w_bitmap_center_draw / 2;
                        float y_circle = y_bitmap_center_draw + h_bitmap_center_draw / 2;
                        canvas.drawCircle(x_circle, y_circle, radius_circle - w_stroke / 2f, paint);
                    } else {
                        //draw border of icon
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(w_border);
                        paint.setColor(color_back_bitmap_center);
                        Rect rect_border = new Rect();
                        int left = (int) (x_bitmap_center_draw + w_border / 2f);
                        int top = (int) (y_bitmap_center_draw + w_border / 2f);
                        int right = (int) (x_bitmap_center_draw + w_bitmap_center_draw - w_border / 2f);
                        int bottom = (int) (y_bitmap_center_draw + h_bitmap_center_draw - w_border / 2f);
                        rect_border.set(left, top, right, bottom);
                        canvas.drawRect(rect_border, paint);
                    }
                }
            }

            if (drawable_left_draw != null) {

                int w_bitmap = drawable_left_draw.getWidth();
                int h_bitmap = drawable_left_draw.getHeight();

                if (percent_bitmap_left == 0) {
                    percent_bitmap_left = 1f;
                }
                h_bitmap_left_draw = h * percent_bitmap_left * 0.5f;
                w_bitmap_left_draw = h_bitmap_left_draw * (w_bitmap / (float) h_bitmap);
                w_bitmap_left_hold = (int) (w_bitmap_left_draw > h_bitmap_left_draw ? w_bitmap_left_draw : h_bitmap_left_draw) + w_space;

                x_bitmap_left_draw = (0) + w_space;
                y_bitmap_left_draw = (h - h_bitmap_left_draw) / 2f;

                //draw icon
                Rect rect_src = new Rect();
                rect_src.set(0, 0, w_bitmap, h_bitmap);
                Rect rect_destination = new Rect();
                rect_destination.set((int) x_bitmap_left_draw, (int) y_bitmap_left_draw, (int) (x_bitmap_left_draw + w_bitmap_left_draw), (int) (y_bitmap_left_draw + h_bitmap_left_draw));
                canvas.drawBitmap(drawable_left_draw, rect_src, rect_destination, paint);
            }
            if (drawable_right_draw != null) {
                int w_bitmap = drawable_right_draw.getWidth();
                int h_bitmap = drawable_right_draw.getHeight();

                if (percent_bitmap_right == 0) {
                    percent_bitmap_right = 1f;
                }
                h_bitmap_right_draw = h * percent_bitmap_right * 0.5f;
                w_bitmap_right_draw = h_bitmap_right_draw * (w_bitmap / (float) h_bitmap);
                w_bitmap_right_hold = (int) (w_bitmap_right_draw > h_bitmap_right_draw ? w_bitmap_right_draw : h_bitmap_right_draw) + w_space;

                x_bitmap_right_draw = (w - w_bitmap_right_draw - w_space);
                y_bitmap_right_draw = (h - h_bitmap_right_draw) / 2f;

                //draw icon
                Rect rect_src = new Rect();
                rect_src.set(0, 0, w_bitmap, h_bitmap);
                Rect rect_destination = new Rect();
                rect_destination.set((int) x_bitmap_right_draw, (int) y_bitmap_right_draw, (int) (x_bitmap_right_draw + w_bitmap_right_draw), (int) (y_bitmap_right_draw + h_bitmap_right_draw));
                canvas.drawBitmap(drawable_right_draw, rect_src, rect_destination, paint);
            }


            //is the text of left need to be drawn?
            if (S.isNotEmpty(text_center)) {
                Paint paint_text = new Paint();
                paint_text.setAntiAlias(true);
                paint_text.setColor(textColor_tmp);
                paint_text.setTextSize(textSize);
                canvas.drawText(text_center, x_text_center_draw, y_text_center_draw, paint_text);

                if (drawTextBorder) {
                    paint_text.setStyle(Paint.Style.STROKE);
                    paint_text.setStrokeWidth(1);
                    paint_text.setColor(color_text_border);
                    paint_text.setAntiAlias(true);
                    canvas.drawText(text_center, x_text_center_draw, y_text_center_draw, paint_text);
                }
            }

            //is the small text of center need to be drawn?
            if (S.isNotEmpty(text_center_small)) {

                if (orientation.equals(vertical)) {

                } else if (orientation.equals(horizontal)) {

                } else {
                    y_text_center_small_draw = (h + h_text_center_small) / 2f;
                }

                Paint paint_text_small = new Paint();
                paint_text_small.setAntiAlias(true);
                paint_text_small.setColor(textColor_small_tmp);
                paint_text_small.setTextSize(textSize_small);
                canvas.drawText(text_center_small, x_text_center_small_draw, y_text_center_small_draw, paint_text_small);

                if (drawTextBorder) {
                    paint_text_small.setStyle(Paint.Style.STROKE);
                    paint_text_small.setStrokeWidth(1);
                    paint_text_small.setColor(color_text_border);
                    paint_text_small.setAntiAlias(true);
                    canvas.drawText(text_center_small, x_text_center_small_draw, y_text_center_small_draw, paint_text_small);
                }
            }
            //is the text of left need to be drawn?
            if (S.isNotEmpty(text_left)) {
                x_text_left_draw = 0 + h_text_left / 2f + w_bitmap_left_hold;
                y_text_left_draw = (h - h_text_left) / 2f + h_text_left;
                y_text_left_draw -= h_text_left / 8f;

                Paint paint_text_left = new Paint();
                paint_text_left.setAntiAlias(true);
                paint_text_left.setColor(textColor_left_tmp);
                paint_text_left.setTextSize(textSize_left);
                canvas.drawText(text_left, x_text_left_draw, y_text_left_draw, paint_text_left);

                if (drawTextBorder) {
                    paint_text_left.setStyle(Paint.Style.STROKE);
                    paint_text_left.setStrokeWidth(1);
                    paint_text_left.setColor(color_text_border);
                    paint_text_left.setAntiAlias(true);
                    canvas.drawText(text_left, x_text_left_draw, y_text_left_draw, paint_text_left);
                }
            }
            //is the text of right need to be drawn?
            if (S.isNotEmpty(text_right)) {
                x_text_right_draw = w - w_text_right - +h_text_right / 2f - w_bitmap_right_hold;
                y_text_right_draw = (h - h_text_right) / 2f + h_text_right;
                y_text_right_draw -= h_text_right / 8f;

                Paint paint_text_right = new Paint();
                paint_text_right.setAntiAlias(true);
                paint_text_right.setColor(textColor_right_tmp);
                paint_text_right.setTextSize(textSize_right);
                canvas.drawText(text_right, x_text_right_draw, y_text_right_draw, paint_text_right);

                if (drawTextBorder) {
                    paint_text_right.setStyle(Paint.Style.STROKE);
                    paint_text_right.setStrokeWidth(1);
                    paint_text_right.setColor(color_text_border);
                    paint_text_right.setAntiAlias(true);
                    canvas.drawText(text_right, x_text_right_draw, y_text_right_draw, paint_text_right);
                }
            }


            if (w_border > 0) {
                int w_rect_border = w;
                int h_rect_border = h;
                int half_border = (int) (w_border / 2);

                float[] radiusOfBorder = new float[4];
                radiusOfBorder[0] = radiusArray[0] - half_border;
                radiusOfBorder[1] = radiusArray[1] - half_border;
                radiusOfBorder[2] = radiusArray[2] - half_border;
                radiusOfBorder[3] = radiusArray[3] - half_border;

                for (int i = 0; i < radiusOfBorder.length; i++) {
                    if (radiusOfBorder[i] < 0) {
                        radiusOfBorder[i] = 0;
                    }
                }

                paint_border.setStyle(Paint.Style.STROKE);
                paint_border.setStrokeWidth(w_border);
                paint_border.setAntiAlias(true);
                //the path
                Path path_border = new Path();
                //radius of left top
                int w_rect_radius_lefttop_border = (int) (radiusOfBorder[0] * 2);
                //radius of right top
                int w_rect_radius_righttop_border = (int) (radiusOfBorder[1] * 2);
                //radius of right bottom
                int w_rect_radius_rightbottom_border = (int) (radiusOfBorder[2] * 2);
                //radius of left bottom
                int w_rect_radius_leftbottom_border = (int) (radiusOfBorder[3] * 2);
                //left top
                path_border.moveTo(half_border, radiusOfBorder[0] + half_border);//0
                //left top
                path_border.arcTo(new RectF(half_border, half_border, w_rect_radius_lefttop_border + half_border, w_rect_radius_lefttop_border + half_border), 180, 90);//1
                //right top
                path_border.lineTo(w_rect_border - radiusOfBorder[1] - half_border, half_border);//2
                //right top
                path_border.arcTo(new RectF(w_rect_border - w_rect_radius_righttop_border - half_border, half_border, w_rect_border - half_border, w_rect_radius_righttop_border + half_border), 270, 90);
                //right bottom
                path_border.lineTo(w_rect_border - half_border, h_rect_border - radiusOfBorder[2] - half_border);//4
                //right bottom
                path_border.arcTo(new RectF(w_rect_border - w_rect_radius_rightbottom_border - half_border, h_rect_border - w_rect_radius_rightbottom_border - half_border, w_rect_border - half_border, h_rect_border - half_border), 0, 90);
                //left bottom
                path_border.lineTo(radiusOfBorder[3] + half_border, h_rect_border - half_border);//6
                //left bottom
                path_border.arcTo(new RectF(half_border, h_rect_border - w_rect_radius_leftbottom_border - half_border, w_rect_radius_leftbottom_border + half_border, h_rect_border - half_border), 90, 90);
                //left top
                path_border.lineTo(half_border, radiusOfBorder[0] + half_border);

                canvas.drawPath(path_border, paint_border);

            }
            drawCanvas(canvas);
            //lines for debug
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(1);
//        canvas.drawLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2,
// paint);
//        canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight(),
// paint);
        }

    }

    @Override
    public void destroyDrawingCache() {
        super.destroyDrawingCache();
        stopAnimation();
        clearFriend();
        showProgress(false);
    }
}
