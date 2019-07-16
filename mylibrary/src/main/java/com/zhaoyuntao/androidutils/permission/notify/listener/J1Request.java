/*
 * Copyright 2018 Zhenjie Yan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhaoyuntao.androidutils.permission.notify.listener;

import com.zhaoyuntao.androidutils.permission.RequestExecutor;
import com.zhaoyuntao.androidutils.permission.source.Source;

/**
 * Created by Zhenjie Yan on 2018/5/29.
 */
class J1Request extends BaseRequest implements RequestExecutor {

    J1Request(Source source) {
        super(source);
    }

    @Override
    public void start() {
        callbackSucceed();
    }

    @Override
    public void execute() {
        // Nothing.
    }

    @Override
    public void cancel() {
        // Nothing.
    }
}