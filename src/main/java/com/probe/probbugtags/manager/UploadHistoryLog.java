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

package com.probe.probbugtags.manager;

import android.content.Context;
import android.content.Intent;

import com.probe.probbugtags.service.UploadFileReportService;
import com.probe.probbugtags.utils.CommonUtil;
import com.probe.probbugtags.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


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

    }

    private void uploadCacheLog(String filePath) {
        File file = new File(filePath);
        if (file.exists() && CommonUtil.isNetworkAvailable(context) && CommonUtil.isNetworkTypeWifi(context)) {
            Logger.i(TAG,"uploadCacheLog");
            Intent intent = new Intent("cacheLog");
            intent.putExtra("filePath", filePath);
            intent.setClass(context.getApplicationContext(), UploadFileReportService.class);
            context.startService(intent);
        }else{
            Logger.i(TAG,"exist cache log, but wifi is not available");
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

//    private Callback<ResponseBody> getCallBack(final String filePath) {
//        return new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.isSuccessful()) {
//                    try {
//                        String body = response.body().string();
//                        MyMessage message = RetrofitClient.parseResp(body);
//
//                        if (message == null) {
//                            return;
//                        }
//                        if (message.getFlag() > 0) {
//                            File file = new File(filePath);
//                            file.delete();
//
//                            if (file.exists()) {
//                                Logger.i(TAG, file.getAbsolutePath() + " delete fail!");
//                            } else {
//                                Logger.i(TAG, file.getAbsolutePath() + " delete success!");
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Logger.e(TAG, "upload filePath info fail");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Logger.e(TAG, "upload filePath info fail");
//            }
//        };
//    }
}
