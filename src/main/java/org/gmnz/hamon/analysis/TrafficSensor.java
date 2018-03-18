package org.gmnz.hamon.analysis;


import org.gmnz.hamon.integration.Dao;

import java.util.concurrent.TimeUnit;


class TrafficSensor implements Runnable {

	long samplingPeriod;
	Dao dao;
	boolean active;
	TrafficSensorListener listener;



	@Override
	public void run() {
		while (active) {
			rest();
			int totalTrafficInTimeWindow = dao.getTotalTrafficInTimeWindow(2);
			listener.notifyTotalTraffic(System.currentTimeMillis(), totalTrafficInTimeWindow);
		}
	}



	private void rest() {
		Thread.yield();
		try {
			TimeUnit.SECONDS.sleep(samplingPeriod);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
