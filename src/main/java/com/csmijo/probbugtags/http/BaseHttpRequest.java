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
import android.util.Base64;

import com.csmijo.probbugtags.BuildConfig;
import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.IOUtils;
import com.csmijo.probbugtags.utils.Logger;




import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import java.util.Map;





public abstract class BaseHttpRequest<T> implements HttpRequest<T> {
    @NonNull
    private final Context context;
    @NonNull
    private final HttpSender.Method method;
    private final String login;
    private final String password;
    private final int connectionTimeOut;
    private final int socketTimeOut;
    private final Map<String, String> headers;

    public BaseHttpRequest( @NonNull Context context, @NonNull HttpSender.Method method,
                           @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers) {
        this.context = context;
        this.method = method;
        this.login = login;
        this.password = password;
        this.connectionTimeOut = connectionTimeOut;
        this.socketTimeOut = socketTimeOut;
        this.headers = headers;
    }


    /**
     * Sends to a URL.
     *
     * @param url     URL to which to send.
     * @param content content to send.
     * @throws IOException if the data cannot be sent.
     */
    @Override
    public void send(@NonNull URL url, @NonNull T content) throws IOException {

        final HttpURLConnection urlConnection = createConnection(url);
        configureTimeouts(urlConnection, connectionTimeOut, socketTimeOut);
        configureHeaders(urlConnection, login, password, headers, content);
        try {
            writeContent(urlConnection, method, content);
            handleResponse(urlConnection.getResponseCode(), urlConnection.getResponseMessage());
            urlConnection.disconnect();
        } catch (SocketTimeoutException e) {
            throw e;
        }
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    protected HttpURLConnection createConnection(@NonNull URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    @SuppressWarnings("WeakerAccess")
    protected void configureTimeouts(@NonNull HttpURLConnection connection, int connectionTimeOut, int socketTimeOut) {
        connection.setConnectTimeout(connectionTimeOut);
        connection.setReadTimeout(socketTimeOut);
    }

    @SuppressWarnings("WeakerAccess")
    protected void configureHeaders(@NonNull HttpURLConnection connection, @Nullable String login, @Nullable String password,
                                    @Nullable Map<String, String> customHeaders, @NonNull T t) throws IOException {
        // Set Headers
        connection.setRequestProperty("User-Agent", String.format("Android probe %1$s", BuildConfig.VERSION_NAME)); //sent ACRA version to server
        connection.setRequestProperty("Accept",
                "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        connection.setRequestProperty("Content-Type", getContentType(context, t));

        // Set Credentials
        if (login != null && password != null) {
            final String credentials = login + ':' + password;
            final String encoded = new String(Base64.encode(credentials.getBytes(Constants.UTF8), Base64.NO_WRAP), Constants.UTF8);
            connection.setRequestProperty("Authorization", "Basic " + encoded);
        }

        if (customHeaders != null) {
            for (final Map.Entry<String, String> header : customHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
    }

    protected abstract String getContentType(@NonNull Context context, @NonNull T t);

    @SuppressWarnings("WeakerAccess")
    protected void writeContent(@NonNull HttpURLConnection connection, @NonNull HttpSender.Method method, @NonNull T content) throws IOException {
        final byte[] contentAsBytes = asBytes(content);
        // write output - see http://developer.android.com/reference/java/net/HttpURLConnection.html
        connection.setRequestMethod(method.name());
        connection.setDoOutput(true);
        connection.setFixedLengthStreamingMode(contentAsBytes.length);

        // Disable ConnectionPooling because otherwise OkHttp ConnectionPool will try to start a Thread on #connect
        System.setProperty("http.keepAlive", "false");

        connection.connect();

        final OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
        try {
            outputStream.write(contentAsBytes);
            outputStream.flush();
        } finally {
            IOUtils.safeClose(outputStream);
        }
    }

    protected abstract byte[] asBytes(T content) throws IOException;

    protected void handleResponse(int responseCode, String responseMessage) throws IOException {
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
            // All is good
            Logger.i("Request received by server", responseMessage);
        } else if (responseCode == HttpURLConnection.HTTP_CLIENT_TIMEOUT || responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            //timeout or server error. Repeat the request later.
            Logger.w( "Could not send ACRA Post responseCode=" + responseCode," message=" + responseMessage);
            throw new IOException("Host returned error code " + responseCode);
        } else if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST && responseCode < HttpURLConnection.HTTP_INTERNAL_ERROR) {
            // Client error. The request must not be repeated. Discard it.
            Logger.w(responseCode + ": Client error - request will be discarded", responseMessage);
        } else {
            Logger.w("Could not send ACRA Post - request will be discarded. responseCode=" + responseCode, " message=" + responseMessage);
        }
    }
}
