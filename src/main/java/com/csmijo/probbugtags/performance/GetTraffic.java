package com.csmijo.probbugtags.performance;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;

import java.util.List;

public class GetTraffic {

	private Context context;
	private String packageName;
	private ConnectivityManager connectivityManager;
	private boolean flag = true;// 是否第一次使用类TrafficDetailInfo
	private TrafficDetailInfo trafficDetailInfo;

	public GetTraffic(Context context) {
		this.context = context;
		this.packageName = context.getPackageName();
		connectivityManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
		IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(netStateChanged, intentFilter);
	}

	/**
	 * 通過trafficDetailInfo获取具体信息； 向类里写值 读取类里的值 先判断是否第一次使用？是：
	 * 
	 * @param trafficDetailInfo
	 */
	public void initTrafDetailInfo(TrafficDetailInfo trafficDetailInfo) {
		this.trafficDetailInfo = trafficDetailInfo;
		setTraffic();
		this.trafficDetailInfo.clearFlowData();
	}

	public void stopTrafDetail() {
		context.unregisterReceiver(netStateChanged);
	}

	public void setTraffic() {
		if (trafficDetailInfo == null) {
			return;
		}
		int uid = getUid();
		if (uid != -1) {
			long receDate = TrafficStats.getUidRxBytes(uid) / 1024;
			long TranDate = TrafficStats.getUidTxBytes(uid) / 1024;
			long totalDate = receDate + TranDate;
			long historyData = trafficDetailInfo.getTotleFlow();
			trafficDetailInfo.setTotleFlow(totalDate);
			long flowGrow = totalDate - historyData;

			long curTime = System.currentTimeMillis();
			long hisTime = trafficDetailInfo.getHistoryTime();
			trafficDetailInfo.setHistoryTime(curTime);
			long timeStamp = (curTime - hisTime) / 1000;

			if (timeStamp == 0) {
				return;
			}

			if (flag) {
				flag = false;
				trafficDetailInfo.setTotleFlow(totalDate);
				trafficDetailInfo.setHistoryTime(System.currentTimeMillis());
			} else {
				// 计算wifi下平均值或流量下平均值
				if (getConnectedType() == ConnectivityManager.TYPE_WIFI) {
					trafficDetailInfo.addWifiTimeStamp(timeStamp);
					trafficDetailInfo.addWifiFlowGrow(flowGrow);
					trafficDetailInfo
							.setWifiPerFlow(trafficDetailInfo.getWifiFlowGrow() / trafficDetailInfo.getWifiTimeStamp());
					//System.out.println("now is using wifi.");
				} else if (getConnectedType() == ConnectivityManager.TYPE_MOBILE) {
					trafficDetailInfo.addMobileTimeStamp(timeStamp);
					trafficDetailInfo.addMobileFlowGrow(flowGrow);
					trafficDetailInfo.setWifiPerFlow(
							trafficDetailInfo.getMobileFlowGrow() / trafficDetailInfo.getMobileTimeStamp());
					//System.out.println("now is using 3G.");
				}
				// 计算wifi和流量模式下，总的平均值
				trafficDetailInfo.addTotalTimeStamp(timeStamp);
				trafficDetailInfo.addTotleFlowGrow(flowGrow);
				trafficDetailInfo
						.setTotalPerFlow(trafficDetailInfo.getTotleFlowGrow() / trafficDetailInfo.getTotalTimeStamp());
			}
		}
	}

	private int getUid() {
		PackageManager packageManager = this.context.getPackageManager();
		List<PackageInfo> packageInfo = packageManager.getInstalledPackages(0);
		for (PackageInfo pckInfo : packageInfo) {
			if (pckInfo.applicationInfo.packageName.equalsIgnoreCase(this.packageName)) {
				return pckInfo.applicationInfo.uid;
			}
		}
		return -1;
	}

	private int getConnectedType() {
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
			return networkInfo.getType();
		}
		return -1;
	}

	public BroadcastReceiver netStateChanged = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				setTraffic();
			}
		}

	};
}
