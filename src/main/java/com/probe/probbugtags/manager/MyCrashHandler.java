/**
 * Cobub Razor
 * <p>
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

package com.csmijo.probbugtags.manager;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;

import com.csmijo.probbugtags.ApplicationInit;
import com.csmijo.probbugtags.baseData.DeviceInfo;
import com.csmijo.probbugtags.collector.LogCatCollector;
import com.csmijo.probbugtags.collector.ThreadCollector;
import com.csmijo.probbugtags.performance.GetMemory;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.Logger;
import com.csmijo.probbugtags.utils.SharedPrefUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;



public class MyCrashHandler implements UncaughtExceptionHandler {
    private final String tag = "MyCrashHandler";
    private static MyCrashHandler myCrashHandler;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private Context context;

    public static MyCrashHandler getInstance() {
        if (myCrashHandler == null) {
            synchronized (MyCrashHandler.class) {
                if (myCrashHandler == null) {
                    myCrashHandler = new MyCrashHandler();
                }
            }
        }
        return myCrashHandler;
    }

    private MyCrashHandler() {
        super();
    }

    public void init(Context context) {
        this.context = context;
        this.defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        if (!handleException(thread, ex) && this.defaultExceptionHandler != null) {
            //如果自己没处理交给系统处理
            this.defaultExceptionHandler.uncaughtException(thread, ex);
        } else {
            // 自己处理，exit app
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }



    private boolean handleException(Thread thread, Throwable ex) {
        if (ex == null) {
            return false;
        }

        /*String errorinfo = getErrorInfo(ex);

        String[] ss = errorinfo.split("\n\t");
        String headstring = "";

        if (ss.length > 2) {
            headstring = ss[0] + "\n\t" + ss[1] + "\n\t" + ss[2];
        } else {
            headstring = ss[0] + "\n\t" + ss[1];
        }

        if (headstring.length() > 255) {
            headstring = headstring.substring(0, 255) + "\n\t";
        } else {
            headstring = headstring + "\n\t";
        }

        String newErrorInfoString = headstring + errorinfo;
*/
        LogCatCollector logCatCollector = new LogCatCollector();
        String[] params = new String[]{"-t", Integer.toString(Constants.DEFAULT_LOGCAT_LINES), "-v", "time"};
        List<String> logcatParams = Arrays.asList(params);
        String logcatInfo = logCatCollector.collectLogCat("main", logcatParams);

        String stackTrace = getStackTrace(ex);

        String threadInfo = ThreadCollector.collect(thread);

        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append(stackTrace);

        JSONObject errorInfo = null;
        try {
            errorInfo = prepareErrorInfoJsonObject(Base64.encodeToString(infoBuilder.toString().getBytes(),Base64.DEFAULT), threadInfo, logcatInfo);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Logger.e(tag, e);

        } catch (Exception e) {
            Logger.e(tag, "lukai " + e.getMessage());
        }

        if (errorInfo != null) {
            // save to file, send next start
            CommonUtil.saveInfoToFile("errorInfo", errorInfo,
                    "/cobub.cache", context);
            Logger.i(tag, "cobub.cache save successed");
        }
        return true;
    }


    private JSONObject prepareErrorInfoJsonObject(String stackTrace, String threadInfo, String logcatInfo)
            throws JSONException {
        JSONObject errorObject = new JSONObject();

        errorObject.put("stacktrace", stackTrace);
        errorObject.put("threadInfo", threadInfo);
        errorObject.put("logcatInfo", logcatInfo);
        errorObject.put("activities", CommonUtil.getActivityName(context));
        Activity activity = ApplicationInit.getCurrentActivity();
        if (null != activity) {
            errorObject.put("activities", activity.getComponentName().getClassName());
        } else {
            errorObject.put("activities", "");
        }

        JSONObject clientInfObject = new ClientdataManager(context)
                .prepareClientdataJSON();
        Iterator<?> it = clientInfObject.keys();
        while (it.hasNext()) {
            String key = it.next().toString();
            errorObject.put(key, clientInfObject.get(key));
        }

        // 新增属性
        long availInternalMem = DeviceInfo.getAvailableInternalMemorySize();
        long totalInternalMem = DeviceInfo.getTotalInternalMemorySize();
        boolean isLowMemory = DeviceInfo.isLowMemory(context);
        long availMem = new GetMemory(context).getAvailMem();
        String orientation = DeviceInfo.getOrientation(context);
        boolean isRooted = DeviceInfo.isRooted();
        boolean isCharging = DeviceInfo.isCharging(context);
        Float batteryLevel = DeviceInfo.getBatteryLevel(context);
        String ipv4Address = DeviceInfo.getLocalIpAddress();
        String[] cpuAbi = DeviceInfo.getCpuAbi();
        StringBuilder cpuAbiBuilder = new StringBuilder();
        for (int i = 0; i < cpuAbi.length - 1; i++) {
            cpuAbiBuilder.append(cpuAbi[i] + ",");
        }
        cpuAbiBuilder.append(cpuAbi[cpuAbi.length - 1]);

        errorObject.put("RomAvailMem", availInternalMem);
        errorObject.put("RomTotalMem", totalInternalMem);
        errorObject.put("isLowMemory ", isLowMemory);
        errorObject.put("RamAvailMem", availMem);
        errorObject.put("orientation", orientation);
        errorObject.put("isRooted", isRooted);
        errorObject.put("isCharging", isCharging);
        errorObject.put("batteryLevel", batteryLevel);
        errorObject.put("ipv4Address", ipv4Address);
        errorObject.put("cpuAbi", cpuAbiBuilder.toString());

        // recent activities
        errorObject.put("recentActivities", SharedPrefUtil.getValue(context, "recent_activity_names", ""));
        // clear sqlite recent activities
        SharedPrefUtil.setValue(context, "recent_activity_names", "");

        return errorObject;
    }

    private String getStackTrace(Throwable ex) {
        Writer result = new StringWriter();
        PrintWriter pw = new PrintWriter(result);

        while (ex != null) {
            ex.printStackTrace(pw);
            ex = ex.getCause();
        }
        String stackTraceStr = result.toString();
        pw.close();

        return stackTraceStr;
    }


}
