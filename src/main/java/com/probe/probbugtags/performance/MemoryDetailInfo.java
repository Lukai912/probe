package com.csmijo.probbugtags.performance;

public class MemoryDetailInfo {

	private int totalPss;
	private int totalprivatedirty;
	private int totalshareddirty;
	private long availMemory, totalMemory;
	private String memRate;
	public int getTotalPss() {
		return totalPss;
	}
	public void setTotalPss(int totalPss) {
		this.totalPss = totalPss;
	}
	public int getTotalprivatedirty() {
		return totalprivatedirty;
	}
	public void setTotalprivatedirty(int totalprivatedirty) {
		this.totalprivatedirty = totalprivatedirty;
	}
	public int getTotalshareddirty() {
		return totalshareddirty;
	}
	public void setTotalshareddirty(int totalshareddirty) {
		this.totalshareddirty = totalshareddirty;
	}
	public long getAvailMemory() {
		return availMemory;
	}
	public void setAvailMemory(long availMemory) {
		this.availMemory = availMemory;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public String getMemRate() {
		return memRate;
	}
	public void setMemRate(String memRate) {
		this.memRate = memRate;
	}
}
