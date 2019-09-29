package com.zhaoyuntao.androidutils.tools;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class AvoidActivityResultFragment extends Fragment {
    private static final String TAG = "AvoidActivityResultFragment";
    private CallBack callBack;
    private int requestCode;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AvoidActivityResultFragment.this.requestCode && callBack != null) {
            callBack.onActivityResult(resultCode, data);
        }
    }

    public static AvoidActivityResultFragment getFragment(AppCompatActivity activity) {
        AvoidActivityResultFragment shareFragment = (AvoidActivityResultFragment) activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (shareFragment == null) {
            shareFragment = new AvoidActivityResultFragment();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.beginTransaction().add(shareFragment, TAG).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return shareFragment;
    }

    public void startActivityForResult(Intent intent, int requestCode, CallBack callBack) {
        this.callBack = callBack;
        this.requestCode = requestCode;
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callBack=null;
    }

    public interface CallBack {
        void onActivityResult(int resultCode, Intent data);
    }
}
