package com.zhaoyuntao.androidutils.clockview;


public abstract class AbstractTabAnimation {

    protected final static int LOWER_POSITION = 0;
    protected final static int MIDDLE_POSITION = 1;
    protected final static int UPPER_POSITION = 2;

    protected final TabDigit.Page mTopPage;
    protected final TabDigit.Page mBottomPage;
    protected final TabDigit.Page mMiddlePage;

    //上中下三个page
    protected int state;
    protected int mAlpha = 0;
    protected long mTime = -1;
    protected float mElapsedTime = 1000.0f;

    public AbstractTabAnimation(TabDigit.Page mTopPage, TabDigit.Page mBottomPage, TabDigit.Page mMiddlePage) {
        this.mTopPage = mTopPage;
        this.mBottomPage = mBottomPage;
        this.mMiddlePage = mMiddlePage;
        initState();
    }

    public void start() {
        makeSureCycleIsClosed();
        mTime = System.currentTimeMillis();
    }

    public void sync() {
        makeSureCycleIsClosed();
    }

    public abstract void initState();
    public abstract void initMiddleTab();
    public abstract void run();
    protected abstract void makeSureCycleIsClosed();

}
