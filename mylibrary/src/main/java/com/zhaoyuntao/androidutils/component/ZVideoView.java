package com.zhaoyuntao.androidutils.component;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class ZVideoView extends FrameLayout implements TextureView.SurfaceTextureListener {
    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private AudioManager mAudioManager;
    private IMediaPlayer mMediaPlayer;
    private String mUrl;
    private ZScaleBar scaleBar;
    private int MSG_PROFRESS = 0;
    private CallBack callBack;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            double position = mMediaPlayer.getCurrentPosition();
//            S.s("position:" + position);
            long all = mMediaPlayer.getDuration();
//            S.s("all:" + all);
            double percent = 0;
            if (position > 0) {
                percent = position / all;
            }
//            S.s(percent);
            scaleBar.setPercent((float) percent);
            handler.sendEmptyMessageDelayed(MSG_PROFRESS, 100);
        }
    };
    private Handler handler_completed = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            scaleBar.setPercent(1);
        }
    };

    public ZVideoView(Context context) {
        this(context, null);
        initView();
    }

    public ZVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
        initView();
    }

    public ZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        scaleBar = new ZScaleBar(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, B.dip2px(getContext(), 50));
        layoutParams.gravity = Gravity.BOTTOM;
        addView(scaleBar, layoutParams);

        scaleBar.setCallBack(new ZScaleBar.CallBack() {
            @Override
            public void whenScaleStart(float percent) {
                handler.removeMessages(MSG_PROFRESS);
                if (mMediaPlayer != null) {
                    mMediaPlayer.pause();
                }
            }

            @Override
            public void whenScale(float percent) {

            }

            @Override
            public void whenScaleEnd(float percent) {
//                S.s("移动到percent:" + percent);
                seekTo(percent);
                if (mMediaPlayer != null) {
                    mMediaPlayer.start();
                }
                handler.sendEmptyMessage(MSG_PROFRESS);
            }
        });
    }

    public void seekTo(double percent) {
        long during = mMediaPlayer.getDuration();
//        S.s("视频总长度:" + during);
        long seek = (long) (during * percent);
//        S.s("移动到:" + seek);
        mMediaPlayer.seekTo(seek);
    }

    public void setVideo(String url) {
        mUrl = url;
        initAudioManager();
        initMediaPlayer();
        initTextureView();
        addTextureView();
        handler.sendEmptyMessage(MSG_PROFRESS);
        if (callBack != null) {
            callBack.whenStart();
        }
    }

    private void initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioManager.requestAudioFocus(new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build());
            } else {
                mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new IjkMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                    S.s("-------------------------------------------------------------------------onInfo:" + i + " " + i1);
                    S.s(IMediaPlayer.MEDIA_INFO_BUFFERING_END);
//                    S.s("进度:" + mMediaPlayer.getCurrentPosition());
                    if (i == 701) {
                        handler.sendEmptyMessage(MSG_PROFRESS);
                    }
                    return false;
                }
            });
            mMediaPlayer.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
//                    S.s("onCompletion");
                    handler_completed.sendEmptyMessage(MSG_PROFRESS);
                    handler.removeMessages(MSG_PROFRESS);
                    if (callBack != null) {
                        callBack.whenEnd();
                    }
                }
            });
            mMediaPlayer.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
//                    S.s("onBufferingUpdate:" + i);
                }
            });
            mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
//                    S.s("onPrepared");
                }
            });
            mMediaPlayer.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer iMediaPlayer) {
//                    S.s("onSeekComplete");
                }
            });
            mMediaPlayer.setOnTimedTextListener(new IMediaPlayer.OnTimedTextListener() {
                @Override
                public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
//                    S.s("onTimedText");
                }
            });
            mMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
//                    S.s("onVideoSizeChanged" + i + " " + i1 + " " + i2 + " " + i3);
                }
            });
        }

    }


    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(getContext());
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    private void addTextureView() {
        removeView(mTextureView);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(mTextureView, 0, params);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface;
            openMediaPlayer();
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    private void openMediaPlayer() {
        // 屏幕常亮
        setKeepScreenOn(true);

        // 设置dataSource
        try {
            mMediaPlayer.setDataSource(getContext().getApplicationContext(), Uri.parse(mUrl)/*, mHeaders*/);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void destroyDrawingCache() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        handler.removeMessages(MSG_PROFRESS);
        super.destroyDrawingCache();
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void whenStart();

        void whenEnd();
    }
}
