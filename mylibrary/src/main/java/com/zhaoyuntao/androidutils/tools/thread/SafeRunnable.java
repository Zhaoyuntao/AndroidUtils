package com.zhaoyuntao.androidutils.tools.thread;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * created by zhaoyuntao
 * on 2019-10-27
 * description:
 */
public abstract class SafeRunnable implements Runnable {

    private WeakReference<Object> lifeCircleWeakReference;

    public SafeRunnable(android.app.Activity lifeCircle) {
        init(lifeCircle);
    }

    public SafeRunnable(androidx.fragment.app.Fragment lifeCircle) {
        init(lifeCircle);
    }

    public SafeRunnable(android.app.Fragment lifeCircle) {
        init(lifeCircle);
    }

    public SafeRunnable(Context lifeCircle) {
        init(lifeCircle);
    }

    public SafeRunnable(android.view.View lifeCircle) {
        init(lifeCircle);
    }

    private void init(Object lifeCircle) {
        this.lifeCircleWeakReference = new WeakReference<>(lifeCircle);
    }

    protected boolean judgement() {
        Object lifeCircle = lifeCircleWeakReference.get();
        if (lifeCircle == null) {
            return false;
        }
        if (lifeCircle instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) lifeCircle;
            if (activity.isFinishing() || activity.isDestroyed()) {
                return false;
            }
        } else if (lifeCircle instanceof androidx.fragment.app.Fragment) {
            androidx.fragment.app.Fragment fragment = (androidx.fragment.app.Fragment) lifeCircle;
            if (fragment.isRemoving()) {
                return false;
            }
            if (fragment.isDetached() || !fragment.isAdded()) {
                return false;
            } else {
                android.app.Activity activity = fragment.getActivity();
                if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                    return false;
                }
            }
        } else if (lifeCircle instanceof android.app.Fragment) {
            android.app.Fragment fragment = (android.app.Fragment) lifeCircle;
            if (fragment.isRemoving()) {
                return false;
            }
            if (fragment.isDetached() || !fragment.isAdded()) {
                return false;
            } else {
                android.app.Activity activity = fragment.getActivity();
                if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                    return false;
                }
            }
        } else if (lifeCircle instanceof Context) {
            Context context = (Context) lifeCircle;
            return (context.getResources() != null);
        } else if (lifeCircle instanceof android.view.View) {
            android.view.View view = (android.view.View) lifeCircle;
            return view != null;
        } else {
            throw new RuntimeException();
        }
        return true;
    }

    protected abstract void runSafely();

    @Override
    public void run() {
        if (judgement()) {
            runSafely();
        }
    }

}

