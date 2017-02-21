package com.csmijo.probbugtags.collector;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.util.Predicate;
import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Executes logcat commands and collects it's output
 *
 * Created by chengqianqian-xy on 2016/10/14.
 */

public class LogCatCollector {

    public String collectLogCat(String bufferName, List<String> logcatArgumentsList) {
        int myPid = android.os.Process.myPid();
        String myPidStr = null;
        if (myPid > 0) {
            myPidStr = Integer.toString(myPid) + "):";
        }

        List<String> commandLine = new ArrayList<>();
        commandLine.add("logcat");
        if (bufferName != null) {
            commandLine.add("-b");
            commandLine.add(bufferName);
        }

        /**
         * "-t n" argument has been introduced in FroYo (API level 8). For
         * devices with lower API level, we will have to emulate its job.
         */
        int tailCount;
        int tailIndex = logcatArgumentsList.indexOf("-t");
        if (tailIndex > -1 && tailIndex < logcatArgumentsList.size()) {
            tailCount = Integer.parseInt(logcatArgumentsList.get(tailIndex + 1));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                logcatArgumentsList.remove(tailIndex + 1);
                logcatArgumentsList.remove(tailIndex);
                logcatArgumentsList.add("-d");
            }
        } else {
            tailCount = -1;
        }

        LinkedList<String> logcatBuf = new BoundedLinkedList<>(tailCount > 0 ? tailCount : Constants.DEFAULT_LOGCAT_LINES);
        commandLine.addAll(logcatArgumentsList);

        try {
            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new
                    String[commandLine.size()]));

            Log.d(TAG, "collectLogCat: "+ TextUtils.join(" ",commandLine.toArray(new String[commandLine.size()])));
            //Dump stderr to null
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        IOUtils.streamToString(process.getErrorStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            final String finalMyPidStr = myPidStr;
            logcatBuf.add(IOUtils.streamToString(process.getInputStream(), new Predicate<String>() {
                @Override
                public boolean apply(String s) {
                    return finalMyPidStr == null || s.contains(finalMyPidStr);
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logcatBuf.toString();
    }
}
