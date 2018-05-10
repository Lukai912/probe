package com.csmijo.probbugtags.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.csmijo.probbugtags.http.CallbackListner;
import com.csmijo.probbugtags.http.HttpConfig;
import com.csmijo.probbugtags.http.HttpSender;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lukai1 on 2018/4/18.
 */

public abstract class AbstractUploadReportService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AbstractUploadReportService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        onHandleUploadReport(intent);
    }

    protected abstract void onHandleUploadReport(Intent intent);
    protected void uploadCommonLog(final Context context, final String type, final String report, String urlExt, String tag) {
        CallbackListner listner = new CallbackListner() {
            @Override
            public void onResponse(String responseBody) {
                try {
                    JSONObject resObject = new JSONObject(responseBody);
                    int errno = resObject.getInt("errno");
                    if (errno != 0) {
                        CommonUtil.saveInfoToFile(type, new JSONObject(report),
                                "/cobub.cache", context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        HttpSender sender = new HttpSender(HttpSender.Method.POST, urlExt, new HttpConfig());
        sender.setOnResponseListener(listner);
        try {
            sender.send(context, report, tag, null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CommonUtil.saveInfoToFile(type, new JSONObject(report),
                        "/cobub.cache", context);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    protected void uploadFileLog(Context context, String filePath, String report, String urlExt, String tag) {
        if (filePath == null) {
            Logger.d(this.getClass().getName(), "uploadFileLog path is null");
            return;
        }

        final File logFile = new File(filePath);
        List<File> fileList = new ArrayList<File>();
        fileList.add(logFile);
        CallbackListner listner = new CallbackListner() {
            @Override
            public void onResponse(String responseBody) {
                try {
                    JSONObject resObject = new JSONObject(responseBody);
                    int errno = resObject.getInt("errno");
                    if (errno == 0) {
                        logFile.delete();

                        if (logFile.exists()) {
                            Logger.i(this.getClass().getName(), logFile.getAbsolutePath() + " delete fail!");
                        } else {
                            Logger.i(this.getClass().getName(), logFile.getAbsolutePath() + " delete success!");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        HttpSender sender = new HttpSender(HttpSender.Method.POST, urlExt, new HttpConfig());
        sender.setOnResponseListener(listner);
        try {
            sender.send(context, report, tag, fileList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
