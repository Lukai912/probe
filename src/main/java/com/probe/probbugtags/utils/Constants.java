package com.probe.probbugtags.utils;

import com.probe.probbugtags.BugTagAgentReal;

/**
 * Created by chengqianqian-xy on 2016/5/30.
 */
public class Constants {

    public final static int Verbose = 0;// equals Log.VERBOSE, for the verbose info
    public final static int Debug = 1; // equals Log.DEBUG, for some debug information
    public final static int Info = 2; // equals Log.INFO, for less important info
    public final static int Warn = 3; // equals Log.WARN, for some warning info
    public final static int Error = 4; // equals Log.ERROR, for the exceptions errors
    public final static int Nothing = 5;    // no log
    //用于管理悬浮窗
    public static final String loginActivityName = "com.probe.probbugtags.activity.LoginActivity";
    public static final String bugEditActivityName = "com.probe.probbugtags.activity.BugEditActivity";
    public static final String reportActivityName = "com.probe.probbugtags.activity.ReportActivity";
    //BugEditActivity 编辑完成后点击Finish按钮
    public static final String bugEditFinishAction = "com.probe.probbugtags.bugedit.finish";
    //BugEditActivity 编辑内容为空
    public static final String bugEditCancelAction = "com.probe.probbugtags.bugedit.empty";
    //publish悬浮窗点击tick按钮
    public static final String publishTickAction = "com.probe.probbugtags.publishFab.tick";
    //publish悬浮窗点击cross按钮
    public static final String publishCrossAction = "com.probe.probbugtags.publishFab.cross";
    //提交成功
    public static final String finishSelf = "com.probe.probbugtags.picTag.finishSelf";
    /* Set the SDK Logs output. If DebugEnabled == true, the log will be
     output depends on DebugLevel. If DebugEnabled == false, there is
     no any outputs.*/
    public static boolean DebugEnabled = true;
    // Default Log Level is Debug, no log information will be output in Logcat
    public static int DebugLevel = Verbose;
    /* Default settings for continue Session duration. If user quit the app and
     then re-entry the app in 30 seconds, it will be seemed as the same
     session.*/
    public static long kContinueSessionMillis = 30000L; // Default is 30s.
    public static long liveTimeMillis = 12 * 60 * 60 * 1000L; //登录后默认可以存活12小时
    // Default is false, not use GPS data.
    public static boolean mProvideGPSData = false;
    // Default is true, only wifi update
    public static boolean mUpdateOnlyWifi = true;
    /* Report policy: 1 means sent the data to server immediately
     0 means the data will be cached and sent to server when next app's start
     up. Default is 1, real-time*/
    public static BugTagAgentReal.SendPolicy mReportPolicy = BugTagAgentReal.SendPolicy.REALTIME;

    public static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;

    //Default number of latest lines kept from the logcat output
    public static final int DEFAULT_LOGCAT_LINES = 100;

    public static final String UTF8 = "UTF-8";

    public static int mPerFormDataDelay = 5000;
    // Default is false, not open fps view.
    public static boolean isOpenWindow = false;
    public static int DEFAULT_CONNECTION_TIMEOUT = 60000;
    public static int DEFAULT_SOCKET_TIMEOUT = 60000;
    //Server URL prefix
    //for debug
   // public static String urlPrefix = "http://10.16.12.45/tanzhen_client/";
    public static String urlPrefix = "http://117.50.90.221:8082/";
    //public static String urlPrefix = "http://10.18.61.46:8080/";
    //for release
//    public static String urlPrefix = "http://211.151.122.244:80/";
    public static String anrUrlExt = "ums/postAnrLog";
    public static String dumpFileUrlExt = "upload/uploadDumpFile/";
    public static String usingUrlExt = "ums/postActivityLog/";
    public static String cacheUrlExt = "ums/uploadLog/";
    public static String leackCanaryUrlExt = "ums/postLeakcanryLog";
    public static String configUrlExt = "ums/getOnlineConfiguration";
}
