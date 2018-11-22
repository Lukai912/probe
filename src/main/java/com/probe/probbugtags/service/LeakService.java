package com.probe.probbugtags.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.probe.probbugtags.ApplicationInit;
import com.probe.probbugtags.BugTagAgentReal;
import com.probe.probbugtags.baseData.AppInfo;
import com.probe.probbugtags.manager.ClientdataManager;
import com.probe.probbugtags.utils.CommonUtil;
import com.probe.probbugtags.utils.IOUtils;
import com.probe.probbugtags.utils.Logger;
import com.probe.probbugtags.utils.ZipCompress;
import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by lukai1 on 2018/2/28.
 * 重写DisplayLeakService方法，压缩dumpFile、leak告警，减少对原leakcanary的代码改动
 */

public class LeakService extends DisplayLeakService {
    private static String TAG = "LeakService";
    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        Logger.d(TAG, "DisplayLeakService:afterDefaulthandling " + heapDump.heapDumpFile.getName());
        if (!result.leakFound || result.excludedLeak) {
            Logger.i(TAG, "DisplayLeakService: result.leakFound = " + result.leakFound + " ;result.excludedLeak = " + result.excludedLeak);
            return;
        }

        File dumpFile = heapDump.heapDumpFile;
        //上传leak的textInfo
        String dumpFileName = dumpFile.getName().substring(0,dumpFile.getName().indexOf("."));
        dumpFileName = dumpFileName + ".zip";
        Logger.i(TAG,"dumpFileName:" + dumpFileName);
        //由于dumpfile的压缩大概耗时10s左右，耗时过久，调整上报顺序，优先上报泄露告警，提高上报成功率
        postLeakTextInfo(heapDump, result, dumpFileName);


        int fileSize = (int) (dumpFile.length() / 1024 / 1024);
        Logger.i(TAG, "before fileSize = " + fileSize + " M");
        //压缩文件
        int index = dumpFile.getName().indexOf(".hprof");
        String fileNameNoSuffix = dumpFile.getName().substring(0, index);
        String zipFilePath = dumpFile.getParent() + File.separator + fileNameNoSuffix + ".zip";         //压缩文件文件绝对路径
        boolean zipResult = ZipCompress.writeByZipOutputStream(dumpFile.getAbsolutePath(), zipFilePath);

        fileSize = (int) (new File(zipFilePath).length() / 1024 / 1024);
        Logger.i(TAG, "after fileSize = " + fileSize + " M");

        String filePath = dumpFile.getAbsolutePath();
        if (zipResult) {
            dumpFile.delete();
            filePath = zipFilePath;
        }


        File file = new File(filePath);
        //上传leak dump file
        if (fileSize < 60) {
            Intent intent = new Intent("leakdump");
            intent.putExtra("filePath", file.getAbsolutePath());
            intent.setClass(this.getApplicationContext(), UploadFileReportService.class);
            this.startService(intent);
        } else {
            Context context = ApplicationInit.getCurrentActivity();
            if (null != context) {
                Logger.i(TAG, "exist file too large :" + file.getName() + ",size:"+file.length());
            }
            file.delete();
        }


    }

    /**
     * 上传leak text info
     *
     * @param heapDump
     * @param result
     * @param newFileName
     */
    private void postLeakTextInfo(HeapDump heapDump,
                                  AnalysisResult result, String newFileName) {
        Logger.d(TAG,"postLeakTextInfo");
        // use JSONObject to save info
        JSONObject uploadObject = new JSONObject(); // 最终上传的JSONObject
        JSONArray leakArray = new JSONArray();
        JSONObject leakObject = new JSONObject();

        try {
            // leak brief info
            String briefInfo = "";
            if (result.leakFound) {
                if (result.excludedLeak) {
                    briefInfo = briefInfo + "* LEAK CAN BE IGNORED.\n";
                }

                briefInfo = briefInfo + "* " + result.className;
                if (!heapDump.referenceName.equals("")) {
                    briefInfo = briefInfo + " (" + heapDump.referenceName + ")";
                }

                briefInfo = briefInfo + " has leaked:\n"
                        + result.leakTrace.toString() + "\n";

            } else if (result.failure != null) {
                briefInfo = briefInfo + "* FAILURE:\n"
                        + Log.getStackTraceString(result.failure) + "\n";
            } else {
                briefInfo = briefInfo + "* NO LEAK FOUND.\n\n";
            }

            // leak detail info
            String detailInfo = result.leakTrace.toDetailedString();

            leakObject = prepareLeakJsonObject(newFileName, briefInfo,
                    detailInfo);

            // 最终上传的json格式
            leakArray.put(leakObject);
            uploadObject.put("appkey", AppInfo.getAppKey());
            uploadObject.put("leakInfo", leakArray);

        } catch (Exception e) {
            // TODO: handle exception
        }
        if (CommonUtil.getReportPolicyMode(getApplicationContext()) == BugTagAgentReal.SendPolicy.REALTIME
                && CommonUtil.isNetworkAvailable(getApplicationContext())) {
            Logger.d(TAG,"wifi send leak info");

            File file = new File(CommonUtil.getUnapprovedFolder(getApplicationContext()), "leak_"+CommonUtil.getFormatTime(System.currentTimeMillis()));
            String reporter = file.getAbsolutePath();
            try {
                IOUtils.writeStringToFile(file,uploadObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
                CommonUtil.saveInfoToFile("leakInfo", leakObject,
                        "/cobub.cache", getApplicationContext());
                return;
            }
            Intent intent = new Intent("leakcanryLog");
            intent.putExtra("content", reporter);
            intent.setClass(this.getApplicationContext(), UploadCommonReortService.class);
            this.startService(intent);
        } else {
            CommonUtil.saveInfoToFile("leakInfo", leakObject,
                    "/cobub.cache", getApplicationContext());
        }
    }


    private JSONObject prepareLeakJsonObject(String newFileName, String
            briefInfo, String
                                                     detailInfo)
            throws JSONException {

        JSONObject leakObject = new JSONObject();

        // device info
        leakObject.put("brand", Build.BRAND);
        leakObject.put("product", Build.PRODUCT);

        // android info
        leakObject.put("api", String.valueOf(Build.VERSION.SDK_INT));

        // error brief info
        leakObject.put("briefInfo", briefInfo);

        // error detail info
        leakObject.put("detailInfo", detailInfo);

        // other key
        leakObject.put("isfix", String.valueOf(0));

        // newFileName
        leakObject.put("dumpFileName", newFileName);

        // clientData
        JSONObject clientInfObject = new ClientdataManager(
                getApplicationContext()).prepareClientdataJSON();
        Iterator<?> it = clientInfObject.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            leakObject.put(key, clientInfObject.get(key));
        }

        return leakObject;
    }
}
