package com.zhaoyuntao.androidutils.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.T;
import com.zhaoyuntao.androidutils.tools.TimeTrigger;


/**
 * 日志显示控制。
 *
 * @author hj
 * @date 2016年7月29日 上午10:08:21
 */
public class LoggerView extends ScrollView {
    private static final String TAG = "LoggerView";

    private Logger logger;

    private static final String SELECTION_BEGIN_EXP = "\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3}";

    private static final int MIN_ACTION_INTERVAL = 300;

    private static final int MIN_FLING_DISTANCE = 100;

    private static final int MAX_LOG_LINE = 1000;

    private long mLastActionTime;

    private int mActionDownY;

    private boolean mCanDirectAppend = true;

    private boolean mActionStart = false;

    private StringBuffer mLogBuffer = new StringBuffer();


    public LoggerView(Context context) {
        super(context);
        initView(context);
    }

    public LoggerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoggerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        setOverScrollMode(OVER_SCROLL_NEVER);
        logger = new Logger(context);
        logger.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        logger.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        scrollBottomSelf();
                    }
                });
            }
        });
        addView(logger);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (!autoScroll && logger.getMeasuredHeight() == (getScrollY() + getHeight())) {
            setAutoScroll(true);
        }
    }

    public void setTextSize(float size) {
        logger.setTextSize(size);
        logger.postInvalidate();
    }

    public void clearLog() {
        logger.clearLog();
    }

    public void appendLog(String log, String hexColorString) {
        if (!B.isLegalHexColorString(hexColorString)) {
            throw new IllegalArgumentException("illegal hex color String:" + hexColorString);
        }
        String formatLog = String.format("<font color='%s'>%s</font><br/>", hexColorString, log);
        logger.appendHtmlFormatLog(formatLog);
    }

    public void appendLog(String log) {
        logger.appendLog(log);
    }

    public void appendLog(String log, int color) {

        String fontColor = B.toHexColorString(color);
        appendLog(log, fontColor);
    }

    public void setLog(String log, String color) {
        logger.clearLog();
        appendLog(log, color);
    }

    public void setLog(String log, int color) {
        logger.clearLog();
        appendLog(log, color);
    }

    public void setLog(String log) {
        logger.clearLog();
        appendLog(log);
    }

    public void scrollBottom() {
        this.fullScroll(FOCUS_DOWN);
        setAutoScroll(true);
    }

    /**
     * 只允许自己调用的滚动
     */
    private void scrollBottomSelf() {
        if (autoScroll) {
            this.fullScroll(FOCUS_DOWN);
        }
    }

    boolean autoScroll = true;

    private void setAutoScroll(boolean autoScroll) {
        S.ss("set auto:" + autoScroll);
        this.autoScroll = autoScroll;
        mCanDirectAppend = autoScroll;
    }

    private class Logger extends android.support.v7.widget.AppCompatEditText {

        private OnTouchListener mOnTouchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        mTimeTrigger.cancel();
                        mCanDirectAppend = false;
                        mLastActionTime = System.currentTimeMillis();
                        mActionDownY = (int) event.getY();
                        setAutoScroll(false);
                    }
                    break;

                    case MotionEvent.ACTION_MOVE: {
                        setAutoScroll(false);
                        ScrollView parent = (ScrollView) getParent();
                        if (!mActionStart) {
                            long curTime = System.currentTimeMillis();
                            if (curTime - mLastActionTime > MIN_ACTION_INTERVAL) {
                                mActionStart = true;

                                int textOffset = convertToTextOffset(event);
                                TextSelection selection = getTextSelectionByOffset(textOffset, '\n');

                                if (null != selection && selection.isValid()) {
                                    setSelection(selection.start, selection.end);
                                    String selectedText = getSelectedText(selection);

                                    dismissClipWindow();

                                    mClipWindow = new ClipPopupWindow(getContext());
                                    mClipWindow.setClipListener(mClipListener);
                                    mClipWindow.setCopyText(selectedText);

                                    int px = B.dip2px(getContext(), 30);

                                    mClipWindow.showAtLocation(logger, Gravity.NO_GRAVITY, (int) event.getX() - px, (int) event.getY() - px - parent.getScrollY());
                                }
                            }
                        }

                        return true;
                    }

                    case MotionEvent.ACTION_UP: {
                        if (null != mClipWindow && mClipWindow.isShowing()) {
                            // 开始倒计时等待
                            mTimeTrigger.cancel();
                            mTimeTrigger.start();
                        }
                        mActionStart = false;
                    }
                    break;

                    case MotionEvent.ACTION_CANCEL: {
                        if (null != mClipWindow && mClipWindow.isShowing()) {
                            mClipWindow.dismiss();
                            mClipWindow = null;

                            mTimeTrigger.cancel();
                        }
                        mActionStart = false;

                    }
                    break;

                    default:

                        break;
                }
                return false;
            }
        };

        private ClipPopupWindow mClipWindow;

        private ClipPopupWindow.ClipListener mClipListener = new ClipPopupWindow.ClipListener() {

            @Override
            public void onCopy(String text) {
                T.t(getContext(), "已复制到剪切板");
            }

            @Override
            public void onCancel() {
                setSelection(getText().toString().length());
            }
        };

        private TimeTrigger mTimeTrigger = new TimeTrigger(3000);


        private Handler mUIHandler;

        public Logger(Context context) {
            super(context);
            initView();
        }

        public Logger(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initView();
        }

        public Logger(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView();
        }

        @Override
        protected boolean getDefaultEditable() {
            return false;
        }

        public void clearLog() {
            setText(null);
        }

        public void dismissClipWindow() {
            if (null != mClipWindow && mClipWindow.isShowing()) {
                mClipWindow.dismiss();
                mClipWindow = null;
            }
        }

        private void initView() {

            mTimeTrigger.setListener(new TimeTrigger.TriggerListener() {

                @Override
                public void onTrigger() {
                    if (null != mUIHandler) {
                        mUIHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                dismissClipWindow();
//                                if (!mCanDirectAppend) {
//                                    mCanDirectAppend = true;
//                                }
                            }
                        });
                    }
                }

            });

            setFocusable(false);
            setOnTouchListener(mOnTouchListener);
            setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    return true;
                }
            });

            setClickable(false);

            mUIHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public boolean performClick() {
            return super.performClick();
        }

        // 将点击位置转换成文本位置偏移量
        private int convertToTextOffset(MotionEvent event) {
            Layout layout = getLayout();

            int line = layout.getLineForVertical((int) (getScrollY() + event.getY()));
            int offset = layout.getOffsetForHorizontal(line, event.getX());

            return offset;
        }

        private boolean isFlinged(int curY) {
            if (Math.abs(curY - mActionDownY) > MIN_FLING_DISTANCE) {
                return true;
            }

            return false;
        }

        // 将offset转换为选区
        private TextSelection getTextSelectionByOffset(int textOffset, String beginRegExp, String endRegExp) {
            TextSelection selection = new TextSelection();

            String allText = getText().toString();
            if (!(0 < textOffset && textOffset < allText.length())) {
                return null;
            }

            String front = allText.substring(0, textOffset);

            Pattern beginPattern = Pattern.compile(beginRegExp);
            Matcher beginMatcher = beginPattern.matcher(front);

            while (beginMatcher.find()) {
                selection.start = beginMatcher.start();
            }

            if (-1 == selection.start) {
                return null;
            }

            Pattern endPattern = Pattern.compile(endRegExp);
            Matcher endMatcher = endPattern.matcher(allText.substring(textOffset));

            if (endMatcher.find()) {
                selection.end = textOffset + endMatcher.start();
            } else {
                selection.end = allText.length();
            }

            return selection;
        }

        private TextSelection getTextSelectionByOffset(int textOffset, char divideChar) {
            TextSelection selection = new TextSelection();

            String allText = getText().toString();
            if (!(0 < textOffset && textOffset < allText.length())) {
                return null;
            }

            int front = textOffset - 1;

            while (front > -1) {
                if (allText.charAt(front) == divideChar) {
                    break;
                } else {
                    front--;
                }
            }

            selection.start = front + 1;

            if (-1 == selection.start) {
                return null;
            }

            int rear = textOffset;
            while (rear < allText.length()) {
                if (allText.charAt(rear) == divideChar) {
                    break;
                } else {
                    rear++;
                }
            }

            selection.end = rear;

            return selection;
        }

        private String getSelectedText(TextSelection selection) {
            String allText = getText().toString();

            return allText.substring(selection.start, selection.end);
        }

        /**
         * 追加日志。
         *
         * @param log
         */
        public void appendLog(String log) {
            if (TextUtils.isEmpty(log)) {
                return;
            }

            // synchronized (LoggerView.this)
            {
                if (mCanDirectAppend) {
                    if (0 != mLogBuffer.length()) {
                        append(mLogBuffer.toString());

                        mLogBuffer = new StringBuffer();
                    }
                    append(log);

                    // 限制显示的log行数
                    if (getLineCount() > MAX_LOG_LINE) {
                        String originText = getText().toString();
                        int startIndex = originText.indexOf("\n") + 1;

                        setText(getText().delete(0, startIndex));
                    }

                    // 滑动到最底部
                    // setSelection(getText().length());
                } else {
                    mLogBuffer.append(log);
                }
            }
        }

        private List<String> mHtmlLogsBuffer = new ArrayList<String>();

        @SuppressWarnings("deprecation")
        public void appendHtmlFormatLog(String htmlFormatLog) {
            if (TextUtils.isEmpty(htmlFormatLog)) {
                return;
            }
            // synchronized (LoggerView.this)
            {
                if (mCanDirectAppend) {
                    if (0 != mHtmlLogsBuffer.size()) {
                        for (int i = 0; i < mHtmlLogsBuffer.size(); i++) {
                            Spanned spanned;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                spanned = Html.fromHtml(mHtmlLogsBuffer.get(i), Html.FROM_HTML_MODE_LEGACY);
                            } else {
                                spanned = Html.fromHtml(mHtmlLogsBuffer.get(i));
                            }
                            append(spanned);
                        }
                        mHtmlLogsBuffer.clear();
                    }

                    Spanned spanned = Html.fromHtml(htmlFormatLog);
                    append(spanned);

                    // 限制显示的log行数
                    if (getLineCount() > MAX_LOG_LINE) {
                        String originText = getText().toString();
                        int startIndex = originText.indexOf("\n") + 1;
                        setText(getText().delete(0, startIndex));
                    }

                    // 滑动到最底部
                    // setSelection(getText().length());
                } else {
                    mHtmlLogsBuffer.add(htmlFormatLog);
                }
            }
        }
    }

    // 文字选区
    class TextSelection {
        public int start = -1;

        public int end = 0;

        public boolean isValid() {
            if (-1 == start || 0 == end) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return "start:" + start + ", end=" + end;
        }
    }
}
