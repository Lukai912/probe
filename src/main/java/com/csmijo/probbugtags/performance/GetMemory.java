package com.csmijo.probbugtags.performance;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Service;
import android.content.Context;
import android.os.Debug;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class GetMemory {

	private Context context;
	private ActivityManager activityManager;
	private MemoryInfo outInfo;
	private Long availMem, totalMem;
	private int pss, privateDirty, shareDirty;
	private int[] pids;

	public GetMemory(Context context) {
		// TODO Auto-generated constructor stub
		activityManager = (ActivityManager) context
				.getSystemService(Service.ACTIVITY_SERVICE);
		outInfo = new MemoryInfo();
		activityManager.getMemoryInfo(outInfo);
	}

	/**
	 * 参考
	 * http://blogs.360.cn/blog/%E6%B5%85%E8%B0%88android%E5%BA%94%E7%94%A8%E6%
	 * 80%A7%E8%83%BD%E4%B9%8B%E5%86%85%E5%AD%98/
	 * 
	 * @param context
	 */
	public GetMemory(Context context, int[] pids) {
		this.context = context;
		this.pids = pids;
		activityManager = (ActivityManager) context
				.getSystemService(Service.ACTIVITY_SERVICE);
		outInfo = new MemoryInfo();
		activityManager.getMemoryInfo(outInfo);
	}

	public void initMemDetailInfo(MemoryDetailInfo memoryDetailInfo) {
		if (memoryDetailInfo == null) {
			return;
		}
		memoryDetailInfo.setAvailMemory(getAvailMem());
		memoryDetailInfo.setTotalMemory(getTotalMem());
		memoryDetailInfo.setTotalprivatedirty(getPrivateDirty(pids));
		memoryDetailInfo.setTotalPss(getPss(pids));
		memoryDetailInfo.setTotalshareddirty(getShareDirty(pids));
		memoryDetailInfo.setMemRate(getMemRate());
	}

	/**
	 * 可用的剩余空间
	 * 
	 * @return
	 */
	public long getAvailMem() {
		// 单位Byte
		availMem = outInfo.availMem / 1024 / 1024;
		return availMem;
	}

	/**
	 * RAM空间
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public long getTotalMem() {

		if (android.os.Build.VERSION.SDK_INT >= 16) {
			totalMem = outInfo.totalMem / 1024 / 1024;
			return totalMem;
		} else {
			try {
				FileReader fr = new FileReader("/proc/meminfo");
				BufferedReader bf = new BufferedReader(fr);
				String line = bf.readLine();
				bf.close();
				String[] array = line.split(":");
				if (array.length >= 1) {
					String[] arr = array[1].trim().split(" ");
					totalMem = (Integer.valueOf(arr[0]).longValue()) / 1024 / 1024;
					return totalMem;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return -1;
	}

	public String getMemRate() {
		if (totalMem == 0) {
			getTotalMem();
		}
		if (pss == 0) {
			getPss(pids);
		}
		DecimalFormat dFormat = new DecimalFormat();
		dFormat.setMaximumFractionDigits(2);
		dFormat.setMinimumFractionDigits(2);
		// pss-KB totalMem-MB
		return dFormat
				.format(100 * (((double) pss / 1024) / (double) totalMem))
				+ "%";

	}

	public int getPss(int[] pid) {
		Debug.MemoryInfo[] memoryinfo = activityManager
				.getProcessMemoryInfo(pid);
		pss = memoryinfo[0].getTotalPss();
		return pss;
	}

	public int getPrivateDirty(int[] pid) {
		Debug.MemoryInfo[] memoryinfo = activityManager
				.getProcessMemoryInfo(pid);
		privateDirty = memoryinfo[0].getTotalPrivateDirty();
		return privateDirty;
	}

	public int getShareDirty(int[] pid) {
		Debug.MemoryInfo[] memoryinfo = activityManager
				.getProcessMemoryInfo(pid);
		shareDirty = memoryinfo[0].getTotalSharedDirty();
		return shareDirty;
	}
}
