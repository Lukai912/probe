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
package com.probe.probbugtags.manager;

import android.content.Context;
import android.content.Intent;

import com.probe.probbugtags.baseData.AppInfo;
import com.probe.probbugtags.service.UploadCommonReortService;
import com.probe.probbugtags.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;


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
			Intent intent = new Intent("config");
			intent.putExtra("content", jsonConfig.toString());
			intent.setClass(context.getApplicationContext(), UploadCommonReortService.class);
			context.startService(intent);
		}
	}
}
