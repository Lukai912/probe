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

package com.csmijo.probbugtags.baseData;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.csmijo.probbugtags.utils.Logger;

public class AppInfo {

	private static Context context;
	private static final String TAG = "AppInfo";
	private static final String UMS_APPKEY = "UMS_APPKEY";

	public static void init(Context context) {
		AppInfo.context = context;
	}


	public static String getAppKey() {
		String umsAppkey = "";
		try {
			PackageManager pm = context.getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			if (ai != null) {
				umsAppkey = ai.metaData.getString(UMS_APPKEY);
				if (umsAppkey == null)
					Logger.e(TAG,
							"Could not read UMS_APPKEY meta-data from AndroidManifest.xml.");
			}
		} catch (Exception e) {
			Logger.e(TAG,
					"Could not read UMS_APPKEY meta-data from AndroidManifest.xml.");
			Logger.e(TAG, e);
		}
		return umsAppkey;
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
}
