/**
 * Cobub Razor
 *
 * An open source analytics android sdk for mobile applications
 *
 * @package     Cobub Razor
 * @author      WBTECH Dev Team
 * @copyright   Copyright (c) 2011 - 2015, NanJing Western Bridge Co.,Ltd.
 * @license     http://www.cobub.com/products/cobub-razor/license
 * @link        http://www.cobub.com/products/cobub-razor/
 * @since       Version 0.1
 * @filesource 
 */
package com.csmijo.probbugtags.manager;

import android.content.Context;

import com.csmijo.probbugtags.BugTagAgent;
import com.csmijo.probbugtags.baseData.AppInfo;
import com.csmijo.probbugtags.bean.MyMessage;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Logger;
import com.csmijo.probbugtags.utils.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigManager {
	private Context context;
	private final String TAG = "ConfigManager";

	public ConfigManager(Context context) {
		this.context = context;
	}

	JSONObject prepareConfigJSON() throws JSONException {
		JSONObject jsonConfig = new JSONObject();
		jsonConfig.put("appkey", AppInfo.getAppKey());
		return jsonConfig;
	}

	public void updateOnlineConfig() {
		JSONObject jsonConfig;
		try {
			jsonConfig = prepareConfigJSON();
		} catch (Exception e) {
			return;
		}

		if (CommonUtil.isNetworkAvailable(context)) {

			RetrofitClient.ApiStores apiStores = RetrofitClient.retrofit().create(RetrofitClient.ApiStores.class);
			Call<ResponseBody> call = apiStores.getConfiguration(jsonConfig.toString());
			call.enqueue(new Callback<ResponseBody>() {
				@Override
				public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
					try {
						if(response.isSuccessful()) {
							String body = response.body().string();
							MyMessage message = RetrofitClient.parseResp(body);

							if (message == null) {
								Logger.e(TAG, "getConfiguration response message is null");
								return;
							} else {
								if (message.getFlag() > 0) {
									JSONObject object = new JSONObject(body);

									int isOnlyWifi = object.getInt("updateonlywifi");
									if (isOnlyWifi == 0)
										BugTagAgent.setUpdateOnlyWifi(false);
									else
										BugTagAgent.setUpdateOnlyWifi(true);
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(Call<ResponseBody> call, Throwable t) {
					Logger.e(TAG,t.getMessage());
				}
			});
		}
	}
}
