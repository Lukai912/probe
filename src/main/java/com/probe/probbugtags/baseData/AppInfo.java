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

package com.probe.probbugtags.baseData;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.probe.probbugtags.utils.Logger;

import java.lang.reflect.Field;

public class AppInfo {

	private static Context context;
	private static String APP_KEY="";
	private static final String TAG = "AppInfo";
	private static final String SDK_VERSION = "1.0";

	public static void init(Context context,String key) {
		AppInfo.context = context;
		AppInfo.APP_KEY = key;
	}

	public static String getSdkVersion() {
		return SDK_VERSION;
	}
	public static String getAppKey() {
		if(APP_KEY.isEmpty()){
			Logger.e(TAG,
					"Could not read UMS_APPKEY meta-data from AndroidManifest.xml.");
		}
		return APP_KEY;
	}

	public static String getAppVersion() {
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
			if (versionName == null)
				versionName = "";
		} catch (Exception e) {
			Logger.e(TAG, e.toString());
		}
		return versionName;
	}

	public static String getAppVersionCode() {
		String versionCode = "";

		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pInfo = pm.getPackageInfo(context.getPackageName(), 0);
			versionCode = String.valueOf(pInfo.versionCode);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return versionCode;
	}
	private static String getBuildConfigValue(String fieldName) {
		String tmp = "null";
		try {
			Class<?> clazz = Class.forName(context.getPackageName() + ".BuildConfig");
			Field field = clazz.getField(fieldName);
			tmp = (String)field.get(null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return tmp;
	}
	public static String getBuildTag(){
		return getBuildConfigValue("buildTag");
	}
	public static String getBuildId(){
		return getBuildConfigValue("buildId");
	}
	public static String getBuildDes(){
		return getBuildConfigValue("buildDes");
	}
	public static String getSubmitId(){
		return getBuildConfigValue("submitId");
	}
}
