/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.csmijo.probbugtags.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.csmijo.probbugtags.utils.Constants;

import java.io.IOException;
import java.util.Map;

/**
 * @author F43nd1r
 * @since 10.03.2017
 */

public class DefaultHttpRequest extends BaseHttpRequest<String> {

    public DefaultHttpRequest(@NonNull Context context, @NonNull HttpSender.Method method,
                              int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers) {
        super(context, method,connectionTimeOut, socketTimeOut, headers);
    }

    @Override
    protected String getContentType(@NonNull Context context, @NonNull String s) {
        return null;
    }

    @Override
    protected byte[] asBytes(String content) throws IOException {
        content = "content=" + content;
        return content.getBytes(Constants.UTF8);
    }
}
