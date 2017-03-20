package com.csmijo.probbugtags.manager;

import android.content.Context;
import android.content.Intent;

import com.csmijo.probbugtags.service.UploadLeakDumpService;
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
            for (final File file : existsFiles) {
                Logger.i(TAG, "exist file :" + file.getName());
                Intent intent = new Intent();
                intent.putExtra("dumpFilePath", file.getAbsolutePath());
                intent.setClass(this.mContext, UploadLeakDumpService.class);
                this.mContext.startService(intent);
            }
        }
    }


    /*private List<File> removeDupli(File[] existsZipFiles, File[] existsHprofFiles) {
        // zip和hprof文件去重
        List<File> existsFiles = new ArrayList<>();
        for (int i = 0; i < existsZipFiles.length; i++) {
            String zipFileNameNoSuffix = getFileNameNoSuffix(existsZipFiles[i], ".zip");

            for (int j = 0; j < existsHprofFiles.length; j++) {
                String hprofFileNameNoSuffix = getFileNameNoSuffix(existsHprofFiles[j], ".hprof");

                if (zipFileNameNoSuffix.equals(hprofFileNameNoSuffix)) {
                    existsZipFiles[i].delete(); // 删除重复的zip文件，上传可能压缩未完成
                } else {
                    existsFiles.add(existsZipFiles[i]);
                }
            }
        }

        // 重新压缩未压缩的hprof文件
        for (File file : existsHprofFiles) {
            String fileNameNoSuffix = getFileNameNoSuffix(file, ".hprof");
            String zipFilePath = file.getParent() + File.separator
                    + fileNameNoSuffix + ".zip"; // 压缩文件文件绝对路径

            boolean zipResult = ZipCompress.writeByZipOutputStream(
                    file.getAbsolutePath(), zipFilePath);

            File zipFile = new File(zipFilePath);
            if (zipResult) {
                file.delete();
                if (zipFile.exists()) {
                    existsFiles.add(zipFile);
                }
            } else {
                // 压缩失败，保存hprof文件
                existsFiles.add(file);
                if (zipFile.exists()) {
                    zipFile.delete();
                }
            }
        }

        return existsFiles;
    }

    private String getFileNameNoSuffix(File file, String suffix) {
        int index = file.getName().indexOf(".zip");
        String fileNameNoSuffix = file.getName().substring(0,
                index);
        return fileNameNoSuffix;
    }*/
}
