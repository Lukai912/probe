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
import com.csmijo.probbugtags.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;


public class HttpSender{

    /**
     * Available HTTP methods to send data. Only POST are currently
     * supported.
     */
    public enum Method {
        POST {
            URL createURL(String baseUrl, String urlExt) throws MalformedURLException {
                return new URL(baseUrl + urlExt);
            }
        };
        abstract URL createURL(String baseUrl, String urlExt) throws MalformedURLException;
    }


    private final Method mMethod;
    private final String mUrlExt;
    private final HttpConfig httpConfig;
    public HttpSender(@Nullable Method method, String urlExt, HttpConfig httpConfig) {
        mMethod = (method == null) ? Method.POST : method;
        mUrlExt = urlExt;
        this.httpConfig = httpConfig;
    }

    public void send(@NonNull Context context, @NonNull String report, @NonNull List<File> attchments) throws Exception {
        try {
            final String baseUrl = Constants.urlPrefix;
            // Adjust URL depending on method
            final URL reportUrl = mMethod.createURL(baseUrl, mUrlExt);
            Logger.d(this.getClass().toString(), "Connect to " + reportUrl);
            sendHttpRequests(context, mMethod, httpConfig.getConnectionTimeout(),
                    httpConfig.getSocketTimeout(), httpConfig.getHttpHeaders(), report, reportUrl, attchments);

        } catch (@NonNull Exception e) {
            throw e;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void sendHttpRequests(@NonNull Context context, @NonNull Method method,
                                    int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                    @NonNull String content, @NonNull URL url, @NonNull List<File> attachments) throws IOException {
        switch (method) {
            case POST:
                if (!httpConfig.isMuiltyPart()) {
                    sendWithoutAttachments(context, method,connectionTimeOut, socketTimeOut, headers, content, url);
                } else {
                    postMultipart(context,connectionTimeOut, socketTimeOut, headers, attachments, url);
                }
                break;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void sendWithoutAttachments(@NonNull Context context, @NonNull Method method,
                                          int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                          @NonNull String content, @NonNull URL url) throws IOException {
        new DefaultHttpRequest(context, method, connectionTimeOut, socketTimeOut, headers).send(url, content);
    }

    @SuppressWarnings("WeakerAccess")
    protected void postMultipart(@NonNull Context context,
                                 int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                 @NonNull List<File> content, @NonNull URL url) throws IOException {
        new MultipartHttpRequest(context, Method.POST, connectionTimeOut, socketTimeOut, headers).send(url, content);
    }



}