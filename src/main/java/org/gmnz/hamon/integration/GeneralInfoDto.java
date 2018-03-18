package org.gmnz.hamon.integration;


import java.util.HashMap;
import java.util.Map;


public class GeneralInfoDto {

	private String topTrafficIp;
	private int topTraffic;
	private String topSc200Ip;
	private int topSc200Count;
	private String topSc302Ip;
	private int topSc302Count;
	private String topSc5xxIp;
	private int topSc5xxCount;
	private String topSc4xxIp;
	private int topSc4xxCount;

	private int totalHits;
	private Map<String, Double> methodsFractions;



	public GeneralInfoDto(String topTrafficIp, int topTraffic, String topSc200Ip, int topSc200Count, String topSc302Ip, int topSc302Count, String topSc5xxIp, int topSc5xxCount, String topSc4xxIp, int topSc4xxCount, int totalHits) {
		this.topTrafficIp = topTrafficIp;
		this.topTraffic = topTraffic;
		this.topSc200Ip = topSc200Ip;
		this.topSc200Count = topSc200Count;
		this.topSc302Ip = topSc302Ip;
		this.topSc302Count = topSc302Count;
		this.topSc5xxIp = topSc5xxIp;
		this.topSc5xxCount = topSc5xxCount;
		this.topSc4xxIp = topSc4xxIp;
		this.topSc4xxCount = topSc4xxCount;
		this.totalHits = totalHits;

		methodsFractions = new HashMap<>();
	}



	public String getTopTrafficIp() {
		return topTrafficIp;
	}



	public int getTopTraffic() {
		return topTraffic;
	}



	public String getTopSc200Ip() {
		return topSc200Ip;
	}



	public int getTopSc200Count() {
		return topSc200Count;
	}



	public String getTopSc302Ip() {
		return topSc302Ip;
	}



	public int getTopSc302Count() {
		return topSc302Count;
	}



	public String getTopSc5xxIp() {
		return topSc5xxIp;
	}



	public int getTopSc5xxCount() {
		return topSc5xxCount;
	}



	public String getTopSc4xxIp() {
		return topSc4xxIp;
	}



	public int getTopSc4xxCount() {
		return topSc4xxCount;
	}



	public int getTotalHits() {
		return totalHits;
	}



	public void addHttpMethodFraction(String method, Double fraction) {
		methodsFractions.put(method, fraction);
	}



	public Iterable<String> getHttpMethodsObserved() {
		return methodsFractions.keySet();
	}



	public double getHttpMethodFraction(String method) {
		Double pctg = methodsFractions.get(method);
		if (pctg != null) {
			return pctg;
		} else {
			return -1;
		}
	}



	@Override
	public String toString() {
		return "GeneralInfoDto{" +
				"topTrafficIp='" + topTrafficIp + '\'' +
				", topTraffic=" + topTraffic +
				", topSc200Ip='" + topSc200Ip + '\'' +
				", topSc200Count=" + topSc200Count +
				", topSc302Ip='" + topSc302Ip + '\'' +
				", topSc302Count=" + topSc302Count +
				", topSc5xxIp='" + topSc5xxIp + '\'' +
				", topSc5xxCount=" + topSc5xxCount +
				", topSc4xxIp='" + topSc4xxIp + '\'' +
				", topSc4xxCount=" + topSc4xxCount +
				", totalHits=" + totalHits +
				", methodsFractions=" + methodsFractions +
				'}';
	}
}
