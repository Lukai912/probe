package com.csmijo.probbugtags.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.csmijo.probbugtags.BugTagAgentReal;
import com.csmijo.probbugtags.bean.MyMessage;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Logger;
import com.csmijo.probbugtags.utils.RetrofitClient;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by chengqianqian-xy on 2016/10/31.
 */

public class UploadLeakDumpService extends IntentService {

    private static final String TAG = "UploadLeakDumpService";
    private Context mContext;

    public UploadLeakDumpService() {
        super("uploadLeakDumpFileService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.mContext = this.getApplicationContext();
        String dumpFilePath = intent.getStringExtra("dumpFilePath");
        File dumFile = new File(dumpFilePath);
        Logger.d(TAG,"dumpFilePath:" + dumpFilePath);
        postLeakDumpFile(dumFile);
    }
    public void postLeakDumpFile(final File heapDumpFile) {

        //由于文件较大，仅在wifi的情况下上传
        if (CommonUtil.getReportPolicyMode(mContext) == BugTagAgentReal
                .SendPolicy
                .REALTIME
                && CommonUtil.isNetworkAvailable(mContext) && CommonUtil.isNetworkTypeWifi(mContext)) {

            RetrofitClient.ApiStores apiStores = RetrofitClient.retrofit().create(RetrofitClient.ApiStores.class);

            // create RequestBody instance from file
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), heapDumpFile);

            //MultipartBody.part is used to send also the actual file name
            MultipartBody.Part body = MultipartBody.Part.createFormData("dumpFile", heapDumpFile.getName(), requestFile);

            Call<ResponseBody> call = apiStores.uploadDumpFile(body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        //上传成功，删除本地文件
                        Logger.i(TAG, "upload leak file success");
                        heapDumpFile.delete();

                        if (heapDumpFile.exists()) {
                            Logger.i(TAG, heapDumpFile.getAbsolutePath() + " delete fail");
                        } else {
                            Logger.i(TAG, heapDumpFile.getAbsolutePath() + " delete success");
                        }
                    } else {
                        Logger.i(TAG, "response is null");
                        Toast.makeText(mContext, "dumpFile upload fail!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Logger.i(TAG, "upload fail");
                    Logger.i(TAG, t == null ? null : t.toString());
                    Toast.makeText(mContext, "dumpFile upload fail!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

   /* //提示用户发送
    private static void alertToUpload(File parentFile) {
        // 提示上传或者删除
        final File[] existsFiles = parentFile.listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !file.isDirectory() && (file.getName().endsWith(".zip") || file.getName().endsWith(".hprof"));
                    }
                }
        );

        BugTagAgentReal.handler.post(new Runnable() {
            @Override
            public void run() {
                if (existsFiles != null && existsFiles.length >= 3) {
                    //AlertDialog提示
                    AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationInit.getCurrentActivity());
                    builder.setMessage("存在多个dump文件，请打开wifi并重启App完成上传，否则会删除文件！是否上传？");
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (existsFiles != null) {
                                for (File file : existsFiles) {
                                    Logger.i(TAG, "delete " + file.getName());
                                    file.delete();
                                }
                            }
                        }
                    });
                    builder.show().setCanceledOnTouchOutside(false);
                }
            }
        });
    }*/

}
