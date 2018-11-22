package com.probe.probbugtags.collector;

import android.support.annotation.Nullable;

/**
 * Collects some data identifying a Thread, usually the Thread whick crashed.
 *
 * Created by chengqianqian-xy on 2016/10/14.
 */

public class ThreadCollector {

    private ThreadCollector() {
    }


    /**
     * Convenience method that collects some data identifying a Thread, ususly the Thread which
     * crashed and returns a string containing the thread's id, name, priority and group name.
     *
     * @param thread the thread
     * @return a string representation of the string including the id, name and priority of the thread.
     */
    public static String collect(@Nullable Thread thread) {
        StringBuilder result = new StringBuilder();

        if (thread != null) {
            result.append("id=").append(thread.getId()).append("\n");
            result.append("name=").append(thread.getName()).append("\n");
            result.append("priority=").append(thread.getPriority()).append("\n");
            if (thread.getThreadGroup() != null) {
                result.append("groupName=").append(thread.getThreadGroup().getName()).append("\n");
            }
        } else {
            result.append("No broken thread, this might be a slient exception");
        }
        return result.toString();
    }
}