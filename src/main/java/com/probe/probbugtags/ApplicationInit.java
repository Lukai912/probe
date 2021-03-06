package com.probe.probbugtags;

import android.app.Activity;
import android.app.Application;

import com.probe.probbugtags.manager.ActivityCrumbsManager;
import com.probe.probbugtags.manager.hookManager.LifecycleAgent;
import com.probe.probbugtags.utils.Constants;

import java.io.FileInputStream;
import java.io.IOException;

public class ApplicationInit {

    public static void onCreateInit(Application mApplication,String key) {

        String processName = getCurrentProcessName();

        if (null != processName) {
            boolean defaultProcess = processName.equalsIgnoreCase(mApplication
                    .getPackageName());
            BugTagAgentReal.init(mApplication.getApplicationContext(),key);
            LifecycleAgent.init(mApplication);
            if (defaultProcess) {

                BugTagAgentReal.setDebugEnabled(true);
                BugTagAgentReal.setDebugLevel(Constants.Verbose);
                BugTagAgentReal.updateOnlineConfig(mApplication.getApplicationContext());
                BugTagAgentReal.postOnInit(mApplication.getApplicationContext());
//                BugTagAgentReal.startPerformService(mApplication.getApplicationContext());
//                BugTagAgentReal.startBugTagFab(mApplication.getApplicationContext());

            }
        }
    }
    /**
     * 返回当前的进程名
     *
     * @return
     */
    public static String getCurrentProcessName() {
        FileInputStream in = null;
        try {
            String fn = "/proc/self/cmdline";
            in = new FileInputStream(fn);
            byte[] buffer = new byte[256];
            int len = 0;
            int b;
            while ((b = in.read()) > 0 && len < buffer.length) {
                buffer[len++] = (byte) b;
            }
            if (len > 0) {
                return new String(buffer, 0, len, "UTF-8");
            }
        } catch (Throwable e) {
        } finally {

            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }


    public static Activity getCurrentActivity() {
        Activity activity = ActivityCrumbsManager.getInstance().getCurrentActivity();
        return activity;
    }

    public static void setCurrentActivity(Activity mCurrentActivity) {
        ActivityCrumbsManager.getInstance().setCurrentActivity(mCurrentActivity);
    }
}
