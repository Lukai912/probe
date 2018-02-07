/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.leakcanary;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.csmijo.probbugtags.ApplicationInit;
import com.csmijo.probbugtags.BugTagAgentReal;
import com.csmijo.probbugtags.baseData.AppInfo;
import com.csmijo.probbugtags.manager.ClientdataManager;
import com.csmijo.probbugtags.service.UploadLeakDumpService;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Logger;
import com.csmijo.probbugtags.utils.RetrofitClient;
import com.csmijo.probbugtags.utils.ZipCompress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Iterator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION_CODES.HONEYCOMB;


/**
 * You can extend this class and override
 * {#afterDefaultHandling(HeapDump, AnalysisResult, String)} to add custom
 * behavior, e.g. uploading the heap dump.
 */
public class DisplayLeakService extends AbstractAnalysisResultService {
    public static final String TAG = "DisplayLeakService";

    @TargetApi(HONEYCOMB)
    @Override
    protected final void onHeapAnalyzed(HeapDump heapDump, AnalysisResult result) {
      /*  String leakInfo = leakInfo(this, heapDump, result, true);
        if (leakInfo.length() < 4000) {
            Logger.i(TAG, leakInfo);
        } else {
            String[] lines = leakInfo.split("\n");
            for (String line : lines) {
                Logger.i(TAG, line);
            }
        }*/

        if (result.failure == null
                && (!result.leakFound || result.excludedLeak)) {
            afterDefaultHandling(heapDump, result);
            return;
        }

        afterDefaultHandling(heapDump, result);
    }

    /**
     * You can override this method and do a blocking call to a server to upload
     * the leak trace and the heap dump. Don't forget to check
     * {@link AnalysisResult#leakFound} and {@link AnalysisResult#excludedLeak}
     * first.
     */
    @SuppressLint("NewApi")
    protected void afterDefaultHandling(HeapDump heapDump,
                                        AnalysisResult result) {
        Logger.d(TAG, "DisplayLeakService:afterDefaulthandling ");
        if (!result.leakFound || result.excludedLeak) {
            Logger.i(TAG, "DisplayLeakService: result.leakFound = " + result.leakFound + " ;result.excludedLeak = " + result.excludedLeak);
            return;
        }

        File dumpFile = heapDump.heapDumpFile;
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
        } else {
            Context context = ApplicationInit.getCurrentActivity();
            if (null != context) {
                Toast.makeText(context, "dump file zip fail!", Toast.LENGTH_SHORT).show();
            }
        }


        File file = new File(filePath);
        //上传leak dump file
        if (fileSize < 90) {
            Intent intent = new Intent();
            intent.putExtra("dumpFilePath", file.getAbsolutePath());
            intent.setClass(this.getApplicationContext(), UploadLeakDumpService.class);
            this.startService(intent);
        } else {
            Context context = ApplicationInit.getCurrentActivity();
            if (null != context) {
                Toast.makeText(context, "dump file is too large!", Toast.LENGTH_SHORT).show();
            }
            file.delete();
        }

        //上传leak的textInfo
        postLeakTextInfo(heapDump, result, file.getName());

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

        // send info to server
        final JSONObject finalLeakObject = leakObject;
        if (CommonUtil.getReportPolicyMode(getApplicationContext()) == BugTagAgentReal.SendPolicy.REALTIME
                && CommonUtil.isNetworkAvailable(getApplicationContext())) {
            RetrofitClient.ApiStores apiStores = RetrofitClient.retrofit().create(RetrofitClient.ApiStores.class);
            Call<ResponseBody> call = apiStores.uploadLeakcanryLog(uploadObject.toString());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Logger.d(TAG,"postLeakTextInfo sucess");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    CommonUtil.saveInfoToFile("leakInfo", finalLeakObject,
                            "/leakInfo.cache", getApplicationContext());
                }
            });
        } else {
            CommonUtil.saveInfoToFile("leakInfo", leakObject,
                    "/leakInfo.cache", getApplicationContext());
        }
    }


    private JSONObject prepareLeakJsonObject(String newFileName, String
            briefInfo, String
                                                     detailInfo)
            throws JSONException {

        JSONObject leakObject = new JSONObject();
        // package info
        leakObject.put("versionCode", AppInfo.getAppVersionCode());

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
