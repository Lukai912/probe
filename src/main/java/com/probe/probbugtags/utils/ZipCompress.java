package com.probe.probbugtags.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件压缩，不支持中文路径名
 * <p>
 * Created by chengqianqian-xy on 2016/10/24.
 */

public class ZipCompress {

    public static boolean writeByZipOutputStream(String srcPath, String desPath) {
        ZipOutputStream zos = null;
        BufferedInputStream bin = null;
        try {
            //创建输出流
            zos = new ZipOutputStream(new FileOutputStream(desPath));
            //启用压缩
            zos.setMethod(ZipOutputStream.DEFLATED);
            //压缩级别设为最强压缩，但时间要多花一点
            zos.setLevel(Deflater.BEST_COMPRESSION);


            File srcFile = new File(srcPath);
            if (!srcFile.exists() && (srcFile.isDirectory() && srcFile.list().length == 0)) {
                throw new FileNotFoundException("File must exist and ZIP file must have at least one entry.");
            }


            bin = new BufferedInputStream(new FileInputStream(srcFile));
            //开始写入新的ZIP文件条目并将流定位到条目数据的开始处
            ZipEntry zipEntry = new ZipEntry(srcFile.getName());
            zos.putNextEntry(zipEntry);

            int len;
            byte[] buffer = new byte[4096];
            while ((len = bin.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            zos.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (zos != null) {
                    zos.closeEntry();
                }
                if (bin != null) {
                    bin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
