package com.zhaoyuntao.androidutils.permission.bridge;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;


/**
 * created by zhaoyuntao
 * on 2019-11-07
 * description:
 */
public class AvoidActivityResultFragment extends Fragment {
    private static final String TAG = "AvoidActivityResultFragment";
    private final int requestCode = 4242;
    private CallBack callBack;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AvoidActivityResultFragment.this.requestCode && callBack != null) {
            callBack.onActivityResult(resultCode, data);
        }
    }

    //bridge fragment
    public static AvoidActivityResultFragment getFragment(Activity activity) {
        AvoidActivityResultFragment shareFragment = (AvoidActivityResultFragment) activity.getFragmentManager().findFragmentByTag(TAG);
        if (shareFragment == null) {
            shareFragment = new AvoidActivityResultFragment();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction().add(shareFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return shareFragment;
    }

    public void startActivityForResult(Intent intent, CallBack callBack) {
        this.callBack = callBack;
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callBack = null;
    }

    public interface CallBack {
        void onActivityResult(int resultCode, Intent data);
    }
}
