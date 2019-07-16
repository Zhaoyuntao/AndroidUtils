package com.zhaoyuntao.androidutils.clockview;


public final class TabAnimationUp extends AbstractTabAnimation {

    public TabAnimationUp(TabDigit.Page mTopPage, TabDigit.Page mBottomPage, TabDigit.Page mMiddlePage) {
        super(mTopPage, mBottomPage, mMiddlePage);
    }

    @Override
    public void initState() {
        state = LOWER_POSITION;
    }

    @Override
    public void initMiddleTab() { /* nothing to do */ }

    @Override
    public void run() {

        if (mTime == -1) {
            return;
        }

        switch (state) {
            case LOWER_POSITION: {
                mBottomPage.next();
                state = MIDDLE_POSITION;
                break;
            }
            case MIDDLE_POSITION: {
                if (mAlpha > 90) {
                    mMiddlePage.next();
                    state = UPPER_POSITION;
                }
                break;
            }
            case UPPER_POSITION: {
                if (mAlpha >= 180) {
                    mTopPage.next();
                    state = LOWER_POSITION;
                    mTime = -1; // animation finished
                }
                break;
            }
        }

        if (mTime != -1) {
            long delta = (System.currentTimeMillis() - mTime);
            mAlpha = (int) (180 * (1 - (1 * mElapsedTime - delta) / (1 * mElapsedTime)));
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
                mMiddlePage.next();
                state = UPPER_POSITION;
            }
            case UPPER_POSITION: {
                mTopPage.next();
                state = LOWER_POSITION;
                mTime = -1; // animation finished
            }
        }
        mMiddlePage.rotate(180);
    }
}
