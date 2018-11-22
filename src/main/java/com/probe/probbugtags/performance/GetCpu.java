package com.probe.probbugtags.performance;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public class GetCpu {
	
	private DecimalFormat decimalFormat;
	private int pid;
	private Context context;
	private long idleCpu1 = 0;
	private long totalCpu1 = 0;
	private long idleCpu2 = 0;
	private long totalCpu2 = 0;
	private long processCpu1 = 0;
	private long processCpu2 = 0;
	
	/**
	 * String[0]:processCpuRatio
	 * Stirng[1]:totalCpuRatio
	 */
	private String[] cpuRatioInfo = {null, null};
	
	
	public GetCpu(Context context, int pid){
		this.context = context;
		this.pid = pid;
		init();
	}
	
	private void init(){
		decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(2);
		decimalFormat.setMinimumFractionDigits(2);
	}
	/**
	 * String[0]:processCpuRatio
	 * 
	 * Stirng[1]:totalCpuRatio
	 * 
	 * GetCpu的算法应该有问题
	 * 以后优化；
	 */
	public String[] getCpuRatioInfo() {
		
		int time = 3;
		long processSub;
		long totalSub;
		long idleSub;
		
		do{
			readCpuStat();
			processSub = processCpu1 - processCpu2;
			totalSub = totalCpu1 - totalCpu2;
			idleSub = idleCpu1-idleCpu2;
		}while(processSub < 0 || totalSub < 0 || idleSub <0 || totalSub < idleSub || time-- != 0);
		
		cpuRatioInfo[0] = decimalFormat
				.format(100 * ((double)processSub / (double)totalSub)) + "%";
		// totalCpuRatio (totalCpu1-totalCpu2)-(idleCpu1-idleCpu2)/(totalCpu1 - totalCpu2)
		cpuRatioInfo[1] = decimalFormat.format(Math.abs(
				100 * ((double) (totalSub - idleSub) / (double)totalSub))) + "%";
//		System.out.println("cpuRatioInfo: " + cpuRatioInfo[0]+"   "+cpuRatioInfo[1]);
		return cpuRatioInfo;
	}
	
	/**
	 * 在一個很小的间隔内取值，求差值
	 */
	private void readCpuStat(){
		String processPid = String.valueOf(this.pid);
		String cpuStatPath = "/proc/"+processPid+"/stat";
		
		try {
			RandomAccessFile cpuInfo2 = new RandomAccessFile("/proc/stat", "r");
			String[] toks2 = cpuInfo2.readLine().split("\\s+");
			idleCpu2 = Long.parseLong(toks2[4]);
			totalCpu2 = Long.parseLong(toks2[1]) + Long.parseLong(toks2[2])
					+ Long.parseLong(toks2[3]) + Long.parseLong(toks2[4])
					+ Long.parseLong(toks2[6]) + Long.parseLong(toks2[5])
					+ Long.parseLong(toks2[7]);
			cpuInfo2.close();
			RandomAccessFile processCpuInfo2 = new RandomAccessFile(cpuStatPath, "r");
			String line2 = "";
			StringBuffer stringBuffer2 = new StringBuffer();
			while((line2 = processCpuInfo2.readLine()) != null){
				stringBuffer2.append(line2 + "\n");
			}
			String[] tok2 = stringBuffer2.toString().split(" ");
			processCpu2 = Long.parseLong(tok2[13]) + Long.parseLong(tok2[14])
						+ Long.parseLong(tok2[15]) + Long.parseLong(tok2[16]);
			processCpuInfo2.close();
			
			//睡2s，再次取值
			Thread.sleep(1000);
			
			RandomAccessFile cpuInfo1 = new RandomAccessFile("/proc/stat", "r");
			String[] toks1 = cpuInfo1.readLine().split("\\s+");
			idleCpu1 = Long.parseLong(toks1[4]);
			totalCpu1 = Long.parseLong(toks1[1]) + Long.parseLong(toks1[2])
					+ Long.parseLong(toks1[3]) + Long.parseLong(toks1[4])
					+ Long.parseLong(toks1[6]) + Long.parseLong(toks1[5])
					+ Long.parseLong(toks1[7]);
			cpuInfo1.close();
			RandomAccessFile processCpuInfo1 = new RandomAccessFile(cpuStatPath, "r");
			String line1 = "";
			StringBuffer stringBuffer1 = new StringBuffer();
			while((line1 = processCpuInfo1.readLine()) != null){
				stringBuffer1.append(line1 + "\n");
			}
			String[] tok1 = stringBuffer1.toString().split(" ");
			processCpu1 = Long.parseLong(tok1[13]) + Long.parseLong(tok1[14])
						+ Long.parseLong(tok1[15]) + Long.parseLong(tok1[16]);
			processCpuInfo1.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*private boolean dataValidate(){
		boolean flag = true;
		if (processCpu2 == 0 || processCpu1 < processCpu2
				|| totalCpu1 < totalCpu2) {
			flag = false;
			totalCpu2 = totalCpu1;
			processCpu2 = processCpu1;
			idleCpu2 = idleCpu1;
			cpuRatioInfo[0] = "0.00%";
		} else if ((totalCpu1 - totalCpu2) < (processCpu1 - processCpu2)) {
			flag = false;
			totalCpu2 = totalCpu1;
			processCpu2 = processCpu1;
			idleCpu2 = idleCpu1;
			cpuRatioInfo[0] = "100.00%";
		}
		return flag;	
	}*/
	
	/**
	 * /sys/devices/system/cpu/cpu1/online
	 * 该文件只有一个数字，0或1。0表示该核心是offline状态的，1表示该核心是online状态的。
	 * 所以，如果你想关闭这个核心，就把online文件的内容改为“0”；如果想打开该核心，就把文件内容改为“1”。
	 * http://blog.clarkhuang.com/archives/43
	 * @return
	 */
	public String getCoreNum(){
		//此方法是否可行？？？？？
//		Runtime.getRuntime().availableProcessors();
		File dir = new File("/sys/devices/system/cpu");
		File[] fileList = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		});
		
		return String.valueOf(fileList.length);
	}
	
	public String getCpuName() {
		try {
			FileReader fr = new FileReader("/proc/cpuinfo");
			BufferedReader bufferReader = new BufferedReader(fr);
			String line = bufferReader.readLine();
			bufferReader.close();
			if(line!=null) {
				String[] array = line.split(":\\s+", 2);
				return array[1];
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
