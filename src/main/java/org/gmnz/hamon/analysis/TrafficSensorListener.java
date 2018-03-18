package org.gmnz.hamon.analysis;


public interface TrafficSensorListener {
	
	void notifyTotalTraffic(long timestamp, int totalTraffic);
	
}
