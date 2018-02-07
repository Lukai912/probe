package com.csmijo.probbugtags.utils;

import com.csmijo.probbugtags.bean.MyMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 负责网络请求部分
 *
 * Created by chengqianqian-xy on 2016/10/28.
 */

public class RetrofitClient {

    private static Retrofit mRetrofit;

    public static Retrofit retrofit() {
        if (mRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(15,TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false)
                    .build();

            mRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.urlPrefix)
                    .client(okHttpClient)
                    .build();
        }
        return mRetrofit;
    }

    public interface ApiStores{
        @FormUrlEncoded
        @POST("/ums/getOnlineConfiguration")
        Call<ResponseBody> getConfiguration(@Field("content")String queryJson);

        @FormUrlEncoded
        @POST("/ums/postActivityLog")
        Call<ResponseBody> uploadActivityLog(@Field("content") String activityLog);

        @FormUrlEncoded
        @POST("/ums/uploadLog")
        Call<ResponseBody> uploadCacheLog(@Field("content") String cacheLog);

        @FormUrlEncoded
        @POST("/ums/postLeakcanryLog")
        Call<ResponseBody> uploadLeakcanryLog(@Field("content") String leakLog);

       /* @FormUrlEncoded
        @POST("/ums/postFpslog")
        Call<ResponseBody> uploadFpsLog(@Field("content") String fpsLog);*/

        @FormUrlEncoded
        @POST("/ums/moblielogin")
        Call<ResponseBody> login(@Field("content") String params);

        @Multipart
        @POST("/upload/upload_file/")
        Call<ResponseBody> uploadBugInfoAndPic(@Part MultipartBody.Part picFile, @Part("content") RequestBody content);

        @Multipart
        @POST("/upload/upload_dumpfile/")
        Call<ResponseBody> uploadDumpFile(@Part MultipartBody.Part dumpFile);

        @FormUrlEncoded
        @POST("/ums/postAnrLog")
        Call<ResponseBody> uploadAnrLog(@Field("content") String anrLog);
    }


    //返回结果解析
    public static MyMessage parseResp(String response) {
        if (response == null) {
            return null;
        }
        Logger.d("RetrofitClient","response = "+response);
        try {
            JSONObject respObject = new JSONObject(response);
            MyMessage message = new MyMessage();
            int flag = respObject.getInt("flag");
            message.setFlag(flag);
            String msg = respObject.getString("msg");
            message.setMsg(msg);
            return message;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
