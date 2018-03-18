package org.gmnz.hamon.parsing;


import java.util.Date;


public class LogLineDto {

	public String ipAddress;
	public String rfc1413id;
	public String userId;
	public Date timestamp = new Date();
	public String httpMethod;
	public String requestUrl;
	public String section;
	public Integer httpStatusCode;
	public Integer bytesTransferred;




	@Override
	public String toString() {
		return "LogLineDto [ipAddress=" + ipAddress + ", rfc1413id=" + rfc1413id + ", userId=" + userId + ", timestamp="
				+ timestamp + ", httpMethod=" + httpMethod + ", requestUrl=" + requestUrl + ", section=" + section
				+ ", httpStatusCode=" + httpStatusCode + ", bytesTransferred=" + bytesTransferred + "]";
	}



}
