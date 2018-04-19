package com.csmijo.probbugtags.service;

import android.content.Context;
import android.content.Intent;

import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.IOUtils;
import com.csmijo.probbugtags.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lukai1 on 2018/4/18.
 */

public class UploadCommonReortService extends AbstractUploadReportService {
    private static String TAG = "UploadCommonReortService";
    private Context mContext;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UploadCommonReortService() {
        super("UploadCommonReortService");
    }

    @Override
    protected void onHandleUploadReport(Intent intent) {
        this.mContext = this.getApplicationContext();
        String report = intent.getStringExtra("content");
        Logger.d(TAG, "onHandleIntent: " + intent.getAction());
        switch (intent.getAction()) {
            case "anr":
                File anrUnapproved = new File(report);
                String anrJsonDetail = "";
                if(!anrUnapproved.exists()) {
                    return;
                }
                try {
                    anrJsonDetail = IOUtils.streamToString(new FileInputStream(anrUnapproved));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                IOUtils.deleteFile(anrUnapproved);
                if(anrJsonDetail.isEmpty()) {
                    return;
                }
                uploadCommonLog(mContext,"anrInfo", anrJsonDetail, Constants.anrUrlExt, "content");
                break;
            case "config":
                uploadCommonLog(mContext,"config", report, Constants.configUrlExt, "content");
                break;
            case "leakcanryLog":
                File leakUnapproved = new File(report);
                String leakJsonDetail = "";
                if(!leakUnapproved.exists()) {
                    return;
                }
                try {
                    leakJsonDetail = IOUtils.streamToString(new FileInputStream(leakUnapproved));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                IOUtils.deleteFile(leakUnapproved);
                if(leakJsonDetail.isEmpty()) {
                    return;
                }
                uploadCommonLog(mContext,"leakInfo", leakJsonDetail, Constants.leackCanaryUrlExt, "content");
                break;
        }
    }
}
