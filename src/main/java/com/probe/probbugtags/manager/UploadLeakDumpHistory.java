package com.probe.probbugtags.manager;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.probe.probbugtags.service.UploadFileReportService;
import com.probe.probbugtags.utils.CommonUtil;
import com.probe.probbugtags.utils.Logger;
import com.squareup.leakcanary.DefaultLeakDirectoryProvider;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

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
        DefaultLeakDirectoryProvider leakDirectoryProvider = new DefaultLeakDirectoryProvider(mContext);
        final List<File> existsFiles = leakDirectoryProvider.listFiles(new FilenameFilter() {
            @Override public boolean accept(File dir, String filename) {
                return filename.endsWith(".zip");
            }
        });

        if (existsFiles != null && existsFiles.size() > 0) {
       /* int length = 0;
        if (existsFiles.length <= 3) {
            length = existsFiles.length;
        } else {
            length = 3;
        }*/
            int length = existsFiles.size();

            if (CommonUtil.isNetworkAvailable(mContext) && CommonUtil.isNetworkTypeWifi(mContext)) {
                for (int i = 0; i < length; i++) {
                    File file = existsFiles.get(i);
                    if(file.length()> 50000000) {
                        Logger.i(TAG, "exist file too large :" + file.getName() + ",size:"+file.length());
                        break;
                    }
                    Logger.d(TAG, "exist file :" + file.getName());
                    Intent intent = new Intent("leakdump");
                    intent.putExtra("filePath", file.getAbsolutePath());
                    intent.setClass(this.mContext, UploadFileReportService.class);
                    this.mContext.startService(intent);
                }
            }else{
                Logger.i(TAG,"存在 " + existsFiles.size()+ " 个dump文件");
                Toast.makeText(mContext,"存在 "+existsFiles.size() + " 个dump文件，请尽快打开wifi并重启APP进行dump文件上传！",Toast.LENGTH_LONG).show();
            }
        }

    }
}
