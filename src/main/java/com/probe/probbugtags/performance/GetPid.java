package com.probe.probbugtags.performance;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;

import java.util.List;

public class GetPid {
	private Context context;
	private String packageName;
	private ActivityManager activitymanager;
	
	public GetPid(Context context){
		this.context = context;
		this.packageName = context.getPackageName();
	}
	
	public int curPkgPid(){
		activitymanager = (ActivityManager) context.
				getSystemService(Service.ACTIVITY_SERVICE);
		
		List<RunningAppProcessInfo> appProcessList = 
				activitymanager.getRunningAppProcesses();
		
		for(RunningAppProcessInfo rAppInfo : appProcessList){
			if (rAppInfo.processName.equalsIgnoreCase(this.packageName)){
				return rAppInfo.pid;
			}
		}
		return -1;
	}
}
