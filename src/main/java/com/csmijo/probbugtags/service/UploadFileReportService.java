package com.csmijo.probbugtags.service;

import android.content.Context;
import android.content.Intent;

import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.Logger;





/**
 * Created by chengqianqian-xy on 2016/10/31.
 */

public class UploadFileReportService extends AbstractUploadReportService{

    private static final String TAG = "UploadFileReportService";
    private Context mContext;

    public UploadFileReportService() {
        super("uploadLeakDumpFileService");
    }

    @Override
    protected void onHandleUploadReport(Intent intent) {
        this.mContext = this.getApplicationContext();
        String report = intent.getStringExtra("content");
        String filePath = intent.getStringExtra("filePath");
        Logger.d(TAG, "onHandleIntent: " + intent.getAction());
        switch (intent.getAction()) {
            case "leakdump":
                uploadFileLog(mContext, filePath, report, Constants.dumpFileUrlExt, "dumpFile");
                break;
            case "cacheLog":
                uploadFileLog(mContext, filePath, report, Constants.cacheUrlExt, "cacheFile");
                break;
        }
    }

}
