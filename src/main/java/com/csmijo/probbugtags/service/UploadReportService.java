package com.csmijo.probbugtags.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.csmijo.probbugtags.BugTagAgentReal;
import com.csmijo.probbugtags.http.BaseHttpRequest;
import com.csmijo.probbugtags.http.HttpConfig;
import com.csmijo.probbugtags.http.HttpSender;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.Logger;
import com.csmijo.probbugtags.utils.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;





/**
 * Created by chengqianqian-xy on 2016/10/31.
 */

public class UploadReportService extends IntentService {

    private static final String TAG = "UploadReportService";
    private Context mContext;

    public UploadReportService() {
        super("uploadLeakDumpFileService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.mContext = this.getApplicationContext();

        String report = intent.getStringExtra("content");
        String filePath = intent.getStringExtra("filePath");
        Logger.d(TAG, "onHandleIntent: " + intent.getAction());
        switch (intent.getAction()) {
            case "leakdump":
                uploadFileLog(filePath, report, Constants.dumpFileUrlExt, "dumpFile");
                break;
            case "anr":
                uploadCommonLog("anrInfo", report, Constants.anrUrlExt, "content");
                break;
            case "config":
                uploadCommonLog("config", report, Constants.configUrlExt, "content");
                break;
            case "usingLog":
                uploadCommonLog("activityInfo", report, Constants.usingUrlExt, "content");
                break;
            case "leakcanryLog":
                uploadCommonLog("leakInfo", report, Constants.leackCanaryUrlExt, "content");
                break;
            case "cacheLog":
                uploadFileLog(filePath, report, Constants.cacheUrlExt, "cacheFile");
                break;
        }

    }

    protected void uploadCommonLog(String type, String report, String urlExt, String tag) {
        HttpSender sender = new HttpSender(HttpSender.Method.POST, urlExt, new HttpConfig());
        try {
            sender.send(mContext, report, tag, null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CommonUtil.saveInfoToFile(type, new JSONObject(report),
                        "/cobub.cache", mContext);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    protected void uploadFileLog(String filePath, String report, String urlExt, String tag) {
        if (filePath == null) {
            Logger.d(TAG, "uploadFileLog path is null");
            return;
        }
        HttpSender sender = new HttpSender(HttpSender.Method.POST, urlExt, new HttpConfig());
        File logFile = new File(filePath);
        List<File> fileList = new ArrayList<File>();
        fileList.add(logFile);
        try {
            sender.send(mContext, report, tag, fileList);
            logFile.delete();

            if (logFile.exists()) {
                Logger.i(TAG, logFile.getAbsolutePath() + " delete fail!");
            } else {
                Logger.i(TAG, logFile.getAbsolutePath() + " delete success!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void postAnrInfo(String anrInfo) {
        Logger.d(TAG, "postAnrInfo");
        HttpSender sender = new HttpSender(HttpSender.Method.POST, Constants.anrUrlExt, new HttpConfig());
        try {
            sender.send(mContext, anrInfo, "content", null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                CommonUtil.saveInfoToFile("anrInfo", new JSONObject(anrInfo),
                        "/anr.cache", mContext);
            } catch (JSONException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void postLeakDumpFile(final File heapDumpFile) {
        HttpSender sender = new HttpSender(HttpSender.Method.POST, Constants.dumpFileUrlExt, new HttpConfig());
        List<File> fileList = new ArrayList<File>();
        fileList.add(heapDumpFile);
        try {
            sender.send(mContext, null, "dumpFile", fileList);
            //上传成功，删除本地文件
            Logger.i(TAG, "upload leak file success");
            heapDumpFile.delete();

            if (heapDumpFile.exists()) {
                Logger.i(TAG, heapDumpFile.getAbsolutePath() + " delete fail");
            } else {
                Logger.i(TAG, heapDumpFile.getAbsolutePath() + " delete success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
