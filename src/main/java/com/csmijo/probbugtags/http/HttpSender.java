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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.attachment.DefaultAttachmentProvider;
import org.acra.config.Configuration;
import org.acra.config.CoreConfiguration;
import org.acra.config.HttpSenderConfiguration;
import org.acra.data.CrashReportData;
import org.acra.data.StringFormat;
import org.acra.http.BinaryHttpRequest;
import org.acra.http.DefaultHttpRequest;
import org.acra.http.MultipartHttpRequest;
import org.acra.util.InstanceCreator;
import org.acra.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;



public class HttpSender{

    /**
     * Available HTTP methods to send data. Only POST and PUT are currently
     * supported.
     */
    public enum Method {
        POST {
            @Override
            URL createURL(String baseUrl, CrashReportData report) throws MalformedURLException {
                return new URL(baseUrl);
            }
        },
        PUT {
            @Override
            URL createURL(String baseUrl, CrashReportData report) throws MalformedURLException {
                return new URL(baseUrl + '/' + report.getString(ReportField.REPORT_ID));
            }
        };

        abstract URL createURL(String baseUrl, CrashReportData report) throws MalformedURLException;
    }

    @NonNull
    private final Uri mFormUri;
    private final Method mMethod;
    private final StringFormat mType;
    @Nullable
    private String mUsername;
    @Nullable
    private String mPassword;


    public HttpSender(@NonNull CoreConfiguration config, @Nullable Method method, @Nullable StringFormat type) {
        this(config, method, type, null);
    }

    public HttpSender(@NonNull CoreConfiguration config, @Nullable Method method, @Nullable StringFormat type, @Nullable String formUri) {
        mMethod = (method == null) ? httpConfig.httpMethod() : method;
    }


    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData report) throws ReportSenderException {
        try {
            final String baseUrl = mFormUri.toString();
            if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Connect to " + baseUrl);

            final String login = mUsername != null ? mUsername : isNull(httpConfig.basicAuthLogin()) ? null : httpConfig.basicAuthLogin();
            final String password = mPassword != null ? mPassword : isNull(httpConfig.basicAuthPassword()) ? null : httpConfig.basicAuthPassword();

            final InstanceCreator instanceCreator = new InstanceCreator();
            final List<Uri> uris = instanceCreator.create(config.attachmentUriProvider(), new DefaultAttachmentProvider()).getAttachments(context, config);

            // Generate report body depending on requested type
            final String reportAsString = convertToString(report, mType);

            // Adjust URL depending on method
            final URL reportUrl = mMethod.createURL(baseUrl, report);

            sendHttpRequests(config, context, mMethod, mType.getMatchingHttpContentType(), login, password, httpConfig.connectionTimeout(),
                    httpConfig.socketTimeout(), httpConfig.httpHeaders(), reportAsString, reportUrl, uris);

        } catch (@NonNull Exception e) {
            throw new ReportSenderException("Error while sending " + config.reportFormat()
                    + " report via Http " + mMethod.name(), e);
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void sendHttpRequests(@NonNull CoreConfiguration configuration, @NonNull Context context, @NonNull Method method, @NonNull String contentType,
                                    @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                    @NonNull String content, @NonNull URL url, @NonNull List<Uri> attachments) throws IOException {
        switch (method) {
            case POST:
                if (attachments.isEmpty()) {
                    sendWithoutAttachments(configuration, context, method, contentType, login, password, connectionTimeOut, socketTimeOut, headers, content, url);
                } else {
                    postMultipart(configuration, context, contentType, login, password, connectionTimeOut, socketTimeOut, headers, content, url, attachments);
                }
                break;
            case PUT:
                sendWithoutAttachments(configuration, context, method, contentType, login, password, connectionTimeOut, socketTimeOut, headers, content, url);
                for (Uri uri : attachments) {
                    putAttachment(configuration, context, login, password, connectionTimeOut, socketTimeOut, headers, url, uri);
                }
                break;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void sendWithoutAttachments(@NonNull CoreConfiguration configuration, @NonNull Context context, @NonNull Method method, @NonNull String contentType,
                                          @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                          @NonNull String content, @NonNull URL url) throws IOException {
        new DefaultHttpRequest(configuration, context, method, contentType, login, password, connectionTimeOut, socketTimeOut, headers).send(url, content);
    }

    @SuppressWarnings("WeakerAccess")
    protected void postMultipart(@NonNull CoreConfiguration configuration, @NonNull Context context, @NonNull String contentType,
                                 @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                 @NonNull String content, @NonNull URL url, @NonNull List<Uri> attachments) throws IOException {
        new MultipartHttpRequest(configuration, context, contentType, login, password, connectionTimeOut, socketTimeOut, headers).send(url, Pair.create(content, attachments));
    }

    @SuppressWarnings("WeakerAccess")
    protected void putAttachment(@NonNull CoreConfiguration configuration, @NonNull Context context,
                                 @Nullable String login, @Nullable String password, int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers,
                                 @NonNull URL url, @NonNull Uri attachment) throws IOException {
        final URL attachmentUrl = new URL(url.toString() + "-" + UriUtils.getFileNameFromUri(context, attachment));
        new BinaryHttpRequest(configuration, context, login, password, connectionTimeOut, socketTimeOut, headers).send(attachmentUrl, attachment);
    }

    /**
     * Convert a report to string
     *
     * @param report the report to convert
     * @param format the format to convert to
     * @return a string representation of the report
     * @throws Exception if conversion failed
     */
    @SuppressWarnings("WeakerAccess")
    protected String convertToString(CrashReportData report, StringFormat format) throws Exception {
        return format.toFormattedString(report, config.reportContent(), "&", "\n", true);
    }

    private boolean isNull(@Nullable String aString) {
        return aString == null || ACRAConstants.NULL_VALUE.equals(aString);
    }

}