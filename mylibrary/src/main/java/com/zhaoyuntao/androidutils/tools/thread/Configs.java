package com.zhaoyuntao.androidutils.tools.thread;

import java.util.concurrent.Executor;

final class Configs {
    String name;// thread name
    Callback callback;// thread callback
    long delay;// delay time
    Executor deliver;// thread deliver
    AsyncCallback asyncCallback;
}
