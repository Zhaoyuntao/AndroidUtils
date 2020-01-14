package com.zhaoyuntao.androidutils.tools.thread;

public interface AsyncCallback<T> {
    void onSuccess(T t);
    void onFailed(Throwable t);
}
