package com.csmijo.probbugtags.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.csmijo.probbugtags.BugTagAgent;
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
        postLeakDumpFile(dumFile);
    }

    public void postLeakDumpFile(final File heapDumpFile) {

        //由于文件较大，仅在wifi的情况下上传
        if (CommonUtil.getReportPolicyMode(mContext) == BugTagAgent
                .SendPolicy
                .REALTIME
                && CommonUtil.isNetworkTypeWifi(mContext)) {

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
                        try {
                            String body = response.body().string();
                            MyMessage message = RetrofitClient.parseResp(body);

                            if (message == null) {
                                Logger.i(TAG, "response is null");
                                Toast.makeText(mContext, "dumpFile upload fail!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (message.getFlag() == 1) {
                                //上传成功，删除本地文件
                                Logger.i(TAG, "upload leak file success");
                                heapDumpFile.delete();

                                if (heapDumpFile.exists()) {
                                    Logger.i(TAG, heapDumpFile.getAbsolutePath() + " delete fail");
                                } else {
                                    Logger.i(TAG, heapDumpFile.getAbsolutePath() + " delete success");
                                }

                            } else {
                                Logger.i(TAG, "response is " + response);
                                Toast.makeText(mContext, "dumpFile upload fail!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Logger.i(TAG, "upload fail");
                    Toast.makeText(mContext, "dumpFile upload fail!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}
