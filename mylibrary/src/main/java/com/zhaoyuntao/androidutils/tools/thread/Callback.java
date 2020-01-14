package com.zhaoyuntao.androidutils.tools.thread;

public interface Callback {

    /**
     * This method will be invoked when thread has been occurs an error.
     *
     * @param threadName The running thread name
     * @param t          The exception
     */
    void onError(String threadName, Throwable t);

    /**
     * notify user to know that it completed.
     *
     * @param threadName The running thread name
     */
    void onCompleted(String threadName);

    /**
     * notify user that task start running
     *
     * @param threadName The running thread name
     */
    void onStart(String threadName);
}
