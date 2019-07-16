package com.zhaoyuntao.androidutils.clockview;


public final class TabAnimationDown extends AbstractTabAnimation {

    public TabAnimationDown(TabDigit.Page mTopPage, TabDigit.Page mBottomPage, TabDigit.Page mMiddlePage) {
        super(mTopPage, mBottomPage, mMiddlePage);
    }

    public void initState() {
        state = UPPER_POSITION;
    }

    @Override
    public void initMiddleTab() {
        mMiddlePage.rotate(180);
    }

    @Override
    public void run() {

        if (mTime == -1) {
            return;
        }

        switch (state) {
            case LOWER_POSITION: {
                if (mAlpha <= 0) {
                    mBottomPage.next();
                    state = UPPER_POSITION;
                    mTime = -1; // animation finished
                }
                break;
            }
            case MIDDLE_POSITION: {
                if (mAlpha < 90) {
                    mMiddlePage.next();
                    state = LOWER_POSITION;
                }
                break;
            }
            case UPPER_POSITION: {
                mTopPage.next();
                state = MIDDLE_POSITION;
                break;
            }
        }

        if (mTime != -1) {
            long delta = (System.currentTimeMillis() - mTime);//过去了多少时间
            mAlpha = 180 - (int) (180 * (1 - (1 * mElapsedTime - delta) / (1 * mElapsedTime)));//计算旋转角度
            mMiddlePage.rotate(mAlpha);
        }

    }

    @Override
    protected void makeSureCycleIsClosed() {
        if (mTime == -1) {
            return;
        }
        switch (state) {
            case LOWER_POSITION: {
                mBottomPage.next();
                state = UPPER_POSITION;
                mTime = -1; // animation finished
            }
        }
        mMiddlePage.rotate(180);
    }
}
