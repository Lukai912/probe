/**
 * Cobub Razor
 * <p/>
 * An open source analytics android sdk for mobile applications
 *
 * @package Cobub Razor
 * @author WBTECH Dev Team
 * @copyright Copyright (c) 2011 - 2015, NanJing Western Bridge Co.,Ltd.
 * @license http://www.cobub.com/products/cobub-razor/license
 * @link http://www.cobub.com/products/cobub-razor/
 * @filesource
 * @since Version 0.1
 */

package com.probe.probbugtags.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.probe.probbugtags.ApplicationInit;
import com.probe.probbugtags.BugTagAgentReal;
import com.probe.probbugtags.baseData.AppInfo;
import com.probe.probbugtags.service.UploadCommonReortService;
import com.probe.probbugtags.utils.CommonUtil;
import com.probe.probbugtags.utils.Logger;
import com.probe.probbugtags.utils.SharedPrefUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Iterator;



public class UsinglogManager {

    private Context context;
    private static final String TAG = "UsinglogManager";
    private String session_id;

    public UsinglogManager() {

    }

    public UsinglogManager(Context context) {
        this.context = context;
    }

    JSONObject prepareUsinglogJSON(String start_millis, String end_millis,
                                   String duration, String activities) throws JSONException {
        JSONObject jsonUsinglog = new JSONObject();

        if (session_id == null) {
            try {
                session_id = CommonUtil.generateSession(context);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        jsonUsinglog.put("session_id", session_id);
        jsonUsinglog.put("start_millis", start_millis);
        jsonUsinglog.put("end_millis", end_millis);
        jsonUsinglog.put("duration", duration);
        jsonUsinglog.put("version", AppInfo.getAppVersion());
        jsonUsinglog.put("activities", activities);

        JSONObject clientInfObject = new ClientdataManager(context)
                .prepareClientdataJSON();
        Iterator<?> it = clientInfObject.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            jsonUsinglog.put(key, clientInfObject.get(key));
        }

        return jsonUsinglog;
    }

    public void onResume() {
        Logger.i(TAG, "Call onResume()");
        try {
            if (CommonUtil.isNewSession(context)) {
                session_id = CommonUtil.generateSession(context);
                Logger.i(TAG, "New Sessionid is " + session_id);
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }

        CommonUtil.saveSessionTime(context);
        Activity activity = ApplicationInit.getCurrentActivity();
        if (null != activity) {
            CommonUtil.savePageName(context, activity.getComponentName().getClassName());
        } else {
            CommonUtil.savePageName(context, "");
        }
    }

    public void onPause() {
        Logger.i(TAG, "Call onPause()");
        String pageName = SharedPrefUtil.getValue(context, "CurrentPage", "");

        long start = SharedPrefUtil.getValue(context, "session_save_time",
                System.currentTimeMillis());
        String start_millis = CommonUtil.getFormatTime(start);

        long end = System.currentTimeMillis();
        String end_millis = CommonUtil.getFormatTime(end);

        String duration = end - start + "";
        CommonUtil.saveSessionTime(context);

        final JSONObject info;
        try {
            info = prepareUsinglogJSON(start_millis, end_millis, duration,
                    pageName);
        } catch (JSONException e) {
            Logger.e(TAG, e);
            return;
        }

        //uploadActivityInfo(info);

    }

    public void onWebPage(String pageName) {
        String lastView = SharedPrefUtil.getValue(context, "CurrentPage", "");
        if (lastView.equals("")) {
            SharedPrefUtil.setValue(context, "CurrentPage", pageName);
            SharedPrefUtil.setValue(context, "session_save_time", System.currentTimeMillis());
        } else {
            long start = SharedPrefUtil.getValue(context, "session_save_time",
                    Long.valueOf(System.currentTimeMillis()));
            String start_millis = CommonUtil.getFormatTime(start);

            long end = System.currentTimeMillis();
            String end_millis = CommonUtil.getFormatTime(end);

            String duration = end - start + "";

            SharedPrefUtil.setValue(context, "CurrentPage", pageName);
            SharedPrefUtil.setValue(context, "session_save_time", end);

            JSONObject obj;
            try {
                // 上传lastView的停留时间
                obj = prepareUsinglogJSON(start_millis, end_millis, duration,
                        lastView);
            } catch (JSONException e) {
                Logger.e(TAG, e);
                return;
            }

            //uploadActivityInfo(obj);
        }
    }

    private void uploadActivityInfo(final JSONObject info) {
        Logger.i(TAG, "uploadActivityInfo");
        if (CommonUtil.getReportPolicyMode(context) == BugTagAgentReal.SendPolicy.REALTIME
                && CommonUtil.isNetworkAvailable(context)) {
            Intent intent = new Intent("usingLog");
            intent.putExtra("content", info.toString());
            intent.setClass(context.getApplicationContext(), UploadCommonReortService.class);
            context.startService(intent);

        } else {
            CommonUtil.saveInfoToFile("activityInfo", info, "/cobub.cache",
                    context);
        }
    }

}