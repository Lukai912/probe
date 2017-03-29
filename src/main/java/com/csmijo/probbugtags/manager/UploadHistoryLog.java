/**
 * Cobub Razor
 * <p/>
 * An open source analytics android sdk for mobile applications
 *
 * @package Cobub Razor
 * @author WBTECH Dev Team
 * @copyright Copyright (c) 2011 - 2015, NanJing Western Bridge Co.,Ltd.
 * @license http://www.cobub.com/products/cobub-razor/license
 * @link http://www.cobub.com/products/cobub-razor/
 * @filesource
 * @since Version 0.1
 */

package com.csmijo.probbugtags.manager;

import android.content.Context;
import android.text.TextUtils;

import com.csmijo.probbugtags.bean.MyMessage;
import com.csmijo.probbugtags.utils.Logger;
import com.csmijo.probbugtags.utils.RetrofitClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadHistoryLog extends Thread {
    public Context context;
    private final String TAG = "UploadHistoryLog";

    public UploadHistoryLog(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void run() {
//        String[] filePaths = new String[]{"/cobub.cache", "/leakInfo.cache", "/fps.cache"};
        String path = context.getCacheDir() + "/cobub.cache";
        uploadCacheLog(path);

        path = context.getCacheDir() + "/leakInfo.cache";
        uploadLeakLog(path);
    }

    private void uploadCacheLog(String filePath) {
        String content = readFile(filePath);
        if (!TextUtils.isEmpty(content)) {
            RetrofitClient.ApiStores apiStores = RetrofitClient.retrofit().create(RetrofitClient.ApiStores.class);
            Call<ResponseBody> call = apiStores.uploadCacheLog(content);
            call.enqueue(getCallBack(filePath));
        }
    }

    private void uploadLeakLog(String filePath) {
        String content = readFile(filePath);
        if (!TextUtils.isEmpty(content)) {
            RetrofitClient.ApiStores apiStores = RetrofitClient.retrofit().create(RetrofitClient.ApiStores.class);
            Call<ResponseBody> call = apiStores.uploadLeakcanryLog(content);
            call.enqueue(getCallBack(filePath));
        }
    }

    private String readFile(String path) {
        File file1 = null;
        FileInputStream in = null;
        try {
            file1 = new File(path);
            if (!file1.exists()) {
                Logger.i(TAG, "No history log file found!");
                return null;
            }
            in = new FileInputStream(file1);
            StringBuffer buffer = new StringBuffer();
            int len = 0;
            byte[] s = new byte[1024 * 4];

            while ((len = in.read(s)) != -1) {
                buffer.append(new String(s, 0, len));
            }
            return buffer.toString();
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Callback<ResponseBody> getCallBack(final String filePath) {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        MyMessage message = RetrofitClient.parseResp(body);

                        if (message == null) {
                            return;
                        }
                        if (message.getFlag() > 0) {
                            File file = new File(filePath);
                            file.delete();

                            if (file.exists()) {
                                Logger.i(TAG, file.getAbsolutePath() + " delete fail!");
                            } else {
                                Logger.i(TAG, file.getAbsolutePath() + " delete success!");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Logger.e(TAG, "upload filePath info fail");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e(TAG, "upload filePath info fail");
            }
        };
    }
}
