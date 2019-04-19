package com.zhaoyuntao.androidutils.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.Button;

public class TimerView extends Button {

	private State mState;
	private int mTotalTime = 60;
	private int mTime;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			int what = msg.what;
			if (what == State.Waiting.ordinal()) {
				if (mState != State.Waiting) {
					return;
				}

				int len = getText().length();
				if (len < 7) {
					append(".");
				} else {
					setText("正在同步数据");
				}
				sendTimeMessage(State.Waiting.ordinal(), 1000);
			} else if (what == State.Timing.ordinal()) {
				if (mTime > 0 && State.Timing.equals(mState)) {
					setText(Html.fromHtml(String.format(
							"<font color=\"#ec5d0f\">%1$d</font>秒后停止保存音频",
							mTime)));
					mTime = mTime - 1;
					sendTimeMessage(State.Timing.ordinal(), 1000);
				} else {
					setState(State.Idle);
				}
			}
		}
	};

	public TimerView(Context context) {
		super(context);
		init();
	}

	public TimerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TimerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public void init() {
		setState(State.Idle);
	}

	private void sendTimeMessage(int what, long delayMillis) {
		mHandler.sendMessageDelayed(mHandler.obtainMessage(what), delayMillis);
	}

	public State getState() {
		return mState;
	}

	public void setState(State state) {
		if (mState == state) {
			return;
		}
		mState = state;
		switch (state) {
		case Idle:
			mTime = mTotalTime;
			setText("保存音频");
			setEnabled(true);
			break;
		case Waiting:
			setText("正在同步数据");
			sendTimeMessage(State.Waiting.ordinal(), 0);
			setEnabled(false);
			break;
		case Timing:
			sendTimeMessage(State.Timing.ordinal(), 0);
			setEnabled(true);
			break;
		default:
			break;
		}
	}

	public void setTotalTime(int totalTime) {
		mTotalTime = totalTime;
		mTime = mTotalTime;
	}

	public static enum State {
		Idle, Waiting, Timing
	}

}
