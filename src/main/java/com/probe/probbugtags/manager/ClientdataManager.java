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

import com.probe.probbugtags.baseData.AppInfo;
import com.probe.probbugtags.baseData.DeviceInfo;
import com.probe.probbugtags.performance.GetMemory;
import com.probe.probbugtags.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientdataManager {
	private final Context context;
	private final String PLATFORM = "android";
	private final String TAG = "ClientdataManager";


	public ClientdataManager(Context context) {
		this.context = context;
	}

	public JSONObject prepareClientdataJSON() throws JSONException {

		JSONObject jsonClientdata = new JSONObject();

		jsonClientdata.put("deviceid", DeviceInfo.getDeviceId());
		jsonClientdata.put("os_version", DeviceInfo.getOsVersion());
		jsonClientdata.put("platform", PLATFORM);
		jsonClientdata.put("language", DeviceInfo.getLanguage());
		jsonClientdata.put("appkey", AppInfo.getAppKey());
		jsonClientdata.put("resolution", DeviceInfo.getResolution());
		jsonClientdata.put("ismobiledevice", true);
		jsonClientdata.put("phonetype", DeviceInfo.getPhoneType());
		jsonClientdata.put("imsi", DeviceInfo.getIMSI());
		jsonClientdata.put("mccmnc", DeviceInfo.getMCCMNC());
		jsonClientdata.put("network", DeviceInfo.getNetworkTypeWIFI2G3G());
		jsonClientdata.put("time", DeviceInfo.getDeviceTime());
		jsonClientdata.put("version", AppInfo.getAppVersion());
		jsonClientdata.put("userid", CommonUtil.getUserIdentifier(context));
//		jsonClientdata.put("userid", "");
		jsonClientdata.put("modulename", DeviceInfo.getDeviceProduct());
		jsonClientdata.put("devicename", DeviceInfo.getDeviceName());
		jsonClientdata.put("wifimac", DeviceInfo.getWifiMac());
	//	jsonClientdata.put("havebt", DeviceInfo.getBluetoothAvailable());
		jsonClientdata.put("havewifi", DeviceInfo.getWiFiAvailable());
	//	jsonClientdata.put("havegps", DeviceInfo.getGPSAvailable());
		jsonClientdata.put("havegravity", DeviceInfo.getGravityAvailable());
		jsonClientdata.put("imei", DeviceInfo.getDeviceIMEI());
		jsonClientdata.put("salt", CommonUtil.getSALT(context));
		jsonClientdata.put("RAM", new GetMemory(context).getTotalMem());
		// package info
		jsonClientdata.put("versionCode", AppInfo.getAppVersionCode());
		jsonClientdata.put("sdk_version", AppInfo.getSdkVersion());

		
	/*	if (Constants.mProvideGPSData) {
			jsonClientdata.put("latitude", DeviceInfo.getLatitude());
			jsonClientdata.put("longitude", DeviceInfo.getLongitude());
		}
*/
		return jsonClientdata;
	}
}
