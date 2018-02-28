package com.csmijo.probbugtags.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.csmijo.probbugtags.bean.BugTag;
import com.csmijo.probbugtags.bean.MyMessage;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.RetrofitClient;
import com.csmijo.probbugtags.utils.SharedPrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by chengqianqian-xy on 2016/7/11.
 */
public class UploadBugAndPicManager {

    private static JSONObject prepareBugJson(List<BugTag> bugTagList, Context context) throws
            JSONException {
        if (null == bugTagList || bugTagList.size() == 0) {
            return null;
        } else {
            JSONObject uploadObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < bugTagList.size(); i++) {
                JSONObject object = bugTagList.get(i).toJsonObject();
                jsonArray.put(object);
            }

            uploadObject.put("bugInfo", jsonArray);
            //在这里添加device相关信息
            JSONObject clientInfObject = new ClientdataManager(context)
                    .prepareClientdataJSON();

            String userName = SharedPrefUtil.getValue(context,"userName", "default");
            clientInfObject.put("userName", userName);
            uploadObject.put("clientData", clientInfObject);

            return uploadObject;
        }
    }


//    public static void postData(List<BugTag> bugTagList, final String picFilePath, Context
//            context, final Handler uploadHandler) {
//        JSONObject bugObject = null;
//        try {
//            bugObject = prepareBugJson(bugTagList, context);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        if (CommonUtil.isNetworkAvailable(context) && bugObject != null && picFilePath != null) {
//            File picFile = new File(picFilePath);
//            // create RequestBody instance from file
//            final RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), picFile);
//
//            // MultipartBody.Part is used to send also the actual file name
//            MultipartBody.Part body = MultipartBody.Part.createFormData("file", picFile.getName(), requestFile);
//
//            // add bug info
//            RequestBody content = RequestBody.create(MediaType.parse("text/plain"),bugObject.toString());
//
//            // finally, execute the request
//            RetrofitClient.ApiStores apiStores = RetrofitClient.retrofit().create(RetrofitClient.ApiStores.class);
//            Call<ResponseBody> call = apiStores.uploadBugInfoAndPic(body,content);
//            call.enqueue(new Callback<ResponseBody>() {
//                @Override
//                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                    if (response.isSuccessful()) {
//                        //解析response，符合约定格式发送成功
//                        try {
//                            String body = response.body().string();
//                            MyMessage respMessage = RetrofitClient.parseResp(body);
//
//                            if (respMessage != null && respMessage.getFlag() == 1) {
//                                deletePicCache(picFilePath);
//                                Message message = uploadHandler.obtainMessage();
//                                message.what = 1;
//                                uploadHandler.sendMessage(message);
//                            } else {
//                                Message message = uploadHandler.obtainMessage();
//                                message.what = -1;
//                                uploadHandler.sendMessage(message);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                    Message message = uploadHandler.obtainMessage();
//                    message.what = -1;
//                    uploadHandler.sendMessage(message);
//                }
//            });
//        } else if (!CommonUtil.isNetworkAvailable(context)) {
//            Toast.makeText(context, "网络不可用", Toast.LENGTH_SHORT).show();
//        }
//    }

    /**
     * 删除图片缓存
     *
     * @param picFilePath
     */
    public static void deletePicCache(String picFilePath) {
        File screenCapPic = new File(picFilePath);
        if (screenCapPic.exists()) {
            screenCapPic.delete();
        }
    }

}
