package com.csmijo.probbugtags.performance;

public class TrafficDetailInfo {
	private long wifiFlowGrow;
	private long wifiPerFlow;
	private long wifiTimeStamp;
	
	private long mobileFlowGrow;
	private long MobilePerFlow;
	private long mobileTimeStamp;
	
	private long totleFlow;
	private long historyTime;
	
	//grow of wifi and mobil
	private long totleFlowGrow;
	//grow of wifi and mobil ------time stamp
	private long totalTimeStamp;
	private long totalPerFlow;
	
	public void clearFlowData(){
		wifiFlowGrow = 0;
		mobileFlowGrow = 0;
		wifiTimeStamp = 0;
		mobileTimeStamp = 0;
	}
	
	
	public long getTotleFlow() {
		return totleFlow;
	}
	public void setTotleFlow(long totleFlow) {
		this.totleFlow = totleFlow;
	}
	public long getWifiFlowGrow() {
		return wifiFlowGrow;
	}
	public void addWifiFlowGrow(long wifiFlowGrow) {
		this.wifiFlowGrow += wifiFlowGrow;
	}
	public long getWifiPerFlow() {
		return wifiPerFlow;
	}
	public void setWifiPerFlow(long wifiPerFlow) {
		this.wifiPerFlow = wifiPerFlow;
	}
	public long getMobileFlowGrow() {
		return mobileFlowGrow;
	}
	public void addMobileFlowGrow(long mobileFlowGrow) {
		this.mobileFlowGrow += mobileFlowGrow;
	}
	public long getMobilePerFlow() {
		return MobilePerFlow;
	}
	public void setMobilePerFlow(long mobilePerFlow) {
		MobilePerFlow = mobilePerFlow;
	}
	public long getWifiTimeStamp() {
		return wifiTimeStamp;
	}
	public void addWifiTimeStamp(long wifiTimeStamp) {
		this.wifiTimeStamp += wifiTimeStamp;
	}
	public long getMobileTimeStamp() {
		return mobileTimeStamp;
	}
	public void addMobileTimeStamp(long mobileTimeStamp) {
		this.mobileTimeStamp += mobileTimeStamp;
	}
	public long getHistoryTime() {
		return historyTime;
	}
	public void setHistoryTime(long historyTime) {
		this.historyTime = historyTime;
	}
	public long getTotleFlowGrow() {
		return totleFlowGrow;
	}
	public void addTotleFlowGrow(long totleFlowGrow) {
		this.totleFlowGrow += totleFlowGrow;
	}
	public long getTotalTimeStamp() {
		return totalTimeStamp;
	}
	public void addTotalTimeStamp(long totalTimeStamp) {
		this.totalTimeStamp += totalTimeStamp;
	}
	public long getTotalPerFlow() {
		return totalPerFlow;
	}
	public void setTotalPerFlow(long totalPerFlow) {
		this.totalPerFlow = totalPerFlow;
	}
}
