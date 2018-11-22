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

package com.probe.probbugtags.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.probe.probbugtags.utils.Constants;
import com.probe.probbugtags.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * lukai
 * 2018
 * 文件上传操作类
 */

public class MultipartHttpRequest extends BaseHttpRequest<List<File>> {

    private static final String BOUNDARY = "%&PROBE_REPORT_DIVIDER&%";
    private static final String BOUNDARY_FIX = "--";
    private static final String NEW_LINE = "\r\n";
    private static final String CONTENT_TYPE = "Content-Type: ";
    @NonNull
    private final Context context;

    public MultipartHttpRequest(@NonNull Context context, @NonNull HttpSender.Method method,
                                int connectionTimeOut, int socketTimeOut, @Nullable Map<String, String> headers) {
        super(context, method,connectionTimeOut, socketTimeOut, headers);
        this.context = context;
    }

    @Override
    protected String getContentType(@NonNull Context context, List<File> content) {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }
    //构造http消息头，将file二进制化
    @Override
    protected byte[] asBytes(String tag, List<File> files) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(outputStream, Constants.UTF8);
        //noinspection TryFinallyCanBeTryWithResources we do not target api 19
        byte[] byteArray = null;
        try {
            for (File file : files) {
                byteArray = fileToByteArray(file);
                Logger.i(Logger.TAG_PREFIX,"byteArray:" + byteArray.length);
                writer.append(NEW_LINE).append(BOUNDARY_FIX).append(BOUNDARY).append(NEW_LINE);
                writer.append("Content-Disposition: form-data; name=\"").append(tag).append('\"').append("; filename=\"").append(file.getName()).append("\"").append(NEW_LINE);
                writer.append(CONTENT_TYPE).append("multipart/form-data").append(NEW_LINE);
                writer.append("Content-Length: ").append(String.valueOf(byteArray.length)).append(NEW_LINE).append(NEW_LINE);
                writer.flush();
                outputStream.write(byteArray);
            }
            writer.append(NEW_LINE).append(BOUNDARY_FIX).append(BOUNDARY).append(BOUNDARY_FIX).append(NEW_LINE);
            writer.flush();
            return outputStream.toByteArray();
        } finally {
            writer.close();
        }
    }
    protected byte[] fileToByteArray(File file) throws IOException {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(Constants.DEFAULT_BUFFER_SIZE_IN_BYTES);
            byte[] b = new byte[Constants.DEFAULT_BUFFER_SIZE_IN_BYTES];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
