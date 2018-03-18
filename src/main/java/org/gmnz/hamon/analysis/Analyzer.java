package org.gmnz.hamon.analysis;


import org.gmnz.hamon.integration.Dao;
import org.gmnz.hamon.integration.GeneralInfoDto;
import org.gmnz.hamon.integration.SectionHitsDto;
import org.gmnz.hamon.output.ConsoleOutput;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * creato da simone in data 13/03/2018.
 */
public class Analyzer implements InfoGathererListener, TrafficSensorListener {


	private ConsoleOutput consoleOutput;
	private InfoGatherer infoGatherer;
	private TrafficSensor trafficSensor;

	private final int alarmThreshold;
	private ExecutorService executor;

	long t0;

	private static final int SECTION_HITS_MAX_LIST = 3;

	private enum AlarmState {
		OFF, ON
	};

	private AlarmState alarmState;




	public Analyzer(Dao dao, int alarmThreshold) {
		this.alarmThreshold = alarmThreshold;
		executor = Executors.newCachedThreadPool();

		consoleOutput = new ConsoleOutput();

		infoGatherer = new InfoGatherer();
		infoGatherer.active = true;
		infoGatherer.listener = this;
		infoGatherer.dao = dao;
		infoGatherer.samplingPeriod = 10; // TODO rendi costante

		trafficSensor = new TrafficSensor();
		trafficSensor.active = true;
		trafficSensor.listener = this;
		trafficSensor.dao = dao;
		trafficSensor.samplingPeriod = 2; // TODO rendi costante

		alarmState = AlarmState.OFF;
	}




	public void start() {
		t0 = System.currentTimeMillis();
		executor.execute(infoGatherer);
		executor.execute(trafficSensor);
		executor.shutdown();
	}




	public void stop() {
		infoGatherer.active = false;
		trafficSensor.active = false;
		consoleOutput.closeConsole();
		try {
			executor.awaitTermination(12, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}




	@Override
	@Deprecated
	public void receiveSectionHitsData(List<SectionHitsDto> sectionHitsDtoList) {
		List<SectionHitsDto> displayData = new ArrayList<>();
		Iterator<SectionHitsDto> i = sectionHitsDtoList.iterator();
		int count = 0;
		while (i.hasNext() && ++count <= SECTION_HITS_MAX_LIST) {
			displayData.add(i.next());
		}
		consoleOutput.displaySectionHitsStats(displayData);
	}




	@Override
	@Deprecated
	public void receiveGeneralInfoData(GeneralInfoDto dto) {
		long tNow = System.currentTimeMillis();
		long elapsed = (tNow - t0) / 1000;
		consoleOutput.displayGeneralInfoData(dto, tNow, elapsed);
	}




	@Override
	public void receiveTrafficStats(List<SectionHitsDto> sectionHitsDtoList, GeneralInfoDto generalInfoDto) {
		long tNow = System.currentTimeMillis();
		long elapsed = (tNow - t0) / 1000;

		List<SectionHitsDto> displayData = new ArrayList<>();
		Iterator<SectionHitsDto> i = sectionHitsDtoList.iterator();
		int count = 0;
		while (i.hasNext() && count < SECTION_HITS_MAX_LIST) {
			displayData.add(i.next());
			count++;
		}
		consoleOutput.updateScreen(displayData, generalInfoDto, tNow, elapsed);
	}




	@Override
	public void notifyTotalTraffic(long timestamp, int totalTraffic) {
		boolean shouldRaiseAlarm = totalTraffic > alarmThreshold;
		double trafficToThresholdRatio = (double) totalTraffic / (double) alarmThreshold;

		if (shouldRaiseAlarm) {
			if (alarmState == AlarmState.OFF) {
				consoleOutput.raiseAlarm(timestamp, totalTraffic, alarmThreshold, trafficToThresholdRatio);
				alarmState = AlarmState.ON;
			}
			/*
			 * this would update the current value of the last alarm notified, but it comes
			 * with a rather different frequency than the normal alarms being raised. This
			 * requires a finer management of the output facilities, which I see as an
			 * improvement.
			 */
			else {
				consoleOutput.updateAlarm(timestamp, totalTraffic, alarmThreshold, trafficToThresholdRatio);
			}
		}
		else
			if (alarmState == AlarmState.ON) {
				consoleOutput.resetAlarm(timestamp, totalTraffic, alarmThreshold, trafficToThresholdRatio);
				alarmState = AlarmState.OFF;
			}
	}



}
