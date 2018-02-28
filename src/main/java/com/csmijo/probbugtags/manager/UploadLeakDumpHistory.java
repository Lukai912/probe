package com.csmijo.probbugtags.manager;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.csmijo.probbugtags.service.UploadReportService;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Logger;
import com.squareup.leakcanary.internal.LeakCanaryInternals;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by chengqianqian-xy on 2016/8/29.
 */
public class UploadLeakDumpHistory extends Thread {

    private Context mContext;
    private static final String TAG = "UploadLeakDumpHistory";

    public UploadLeakDumpHistory(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        super.run();
        File leakCanaryDirectory = LeakCanaryInternals.storageDirectory();
        final File[] existsFiles = leakCanaryDirectory
                .listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !file.isDirectory()
                                && (file.getName().endsWith(".zip") || file.getName().endsWith(".hprof"));
                    }
                });

        if (existsFiles != null && existsFiles.length > 0) {
           /* int length = 0;
            if (existsFiles.length <= 3) {
                length = existsFiles.length;
            } else {
                length = 3;
            }*/
            int length = existsFiles.length;

            if (CommonUtil.isNetworkAvailable(mContext) && CommonUtil.isNetworkTypeWifi(mContext)) {
                for (int i = 0; i < length; i++) {
                    File file = existsFiles[i];
                    Logger.d(TAG, "exist file :" + file.getName());
                    Intent intent = new Intent("leakdump");
                    intent.putExtra("filePath", file.getAbsolutePath());
                    intent.setClass(this.mContext, UploadReportService.class);
                    this.mContext.startService(intent);
                }
            }else{
                Logger.i(TAG,"存在 " + existsFiles.length+ " 个dump文件");
                Toast.makeText(mContext,"存在 "+existsFiles.length + " 个dump文件，请尽快打开wifi并重启APP进行dump文件上传！",Toast.LENGTH_LONG).show();
            }
        }
    }
}
