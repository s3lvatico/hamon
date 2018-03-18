package org.gmnz.hamon.output;


import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.gmnz.hamon.integration.GeneralInfoDto;
import org.gmnz.hamon.integration.SectionHitsDto;
import org.gmnz.util.Formatting;


public class ConsoleOutput {

	private LinkedList<Alarm> alarmsHistory;

	private class Alarm {

		static final int TYPE_RAISED = 1;
		static final int TYPE_RESET = 2;




		Alarm(long ts, int type, String message) {
			this.ts = ts;
			this.type = type;
			this.message = message;
		}

		long ts;
		int type;
		String message;

	}




	public ConsoleOutput() {
		AnsiConsole.systemInstall();
		Ansi.ansi().eraseScreen();
		alarmsHistory = new LinkedList<>();
	}




	@Deprecated
	public void displaySectionHitsStats(List<SectionHitsDto> sectionHitsDtoList) {
		for (SectionHitsDto dto : sectionHitsDtoList) {
			System.out.printf("Section <%s> | hits <%d>%n", dto.getSection(), dto.getHits());
		}
	}




	@Deprecated
	public void displayGeneralInfoData(GeneralInfoDto dto, long timestampNow, long timeSecondsElapsed) {
		System.out.println(".-- ts@" + timestampNow + " | elapsed : " + timeSecondsElapsed + " s");
		System.out.printf("topTraffic <%s> (%d) | total site hits <%d>%n", dto.getTopTrafficIp(), dto.getTopTraffic(),
				dto.getTotalHits());
		System.out.printf("topSc200   <%s> (%d) | topSc302   <%s> (%d) | ", dto.getTopSc200Ip(), dto.getTopSc200Count(),
				dto.getTopSc302Ip(), dto.getTopSc302Count());
		System.out.printf("topSc5xx   <%s> (%d)  |  topSc4xx   <%s> (%d)%n", dto.getTopSc5xxIp(), dto.getTopSc5xxCount(),
				dto.getTopSc4xxIp(), dto.getTopSc4xxCount());
		System.out.print("Request methods breakdown: ");
		for (String method : dto.getHttpMethodsObserved()) {
			System.out.printf("%s : %.2f | ", method, dto.getHttpMethodFraction(method));
		}
		System.out.println("\n'--");
	}




	public void updateScreen(List<SectionHitsDto> sectionHitsDtoList, GeneralInfoDto dto, long timestampNow,
			long timeSecondsElapsed) {
		Ansi a = Ansi.ansi();
		a.eraseScreen();
		a.fgDefault().cursor(24, 0);

		a = buildTrafficStatsOutput(a, timestampNow, timeSecondsElapsed, dto, sectionHitsDtoList);
		AnsiConsole.out.println(a);

		a = buildAlarmListOutput(a);
		AnsiConsole.out.print(a);

		a.restoreCursorPosition().fgDefault();
	}




	private Ansi buildTrafficStatsOutput(Ansi a, long timestampNow, long timeSecondsElapsed, GeneralInfoDto dto,
			List<SectionHitsDto> sectionHitsDtoList) {
		String statsUpdateTimestamp = Formatting.formatClfTimestamp(timestampNow);
		a.a(String.format(".-- status update at %s (%d s elapsed since start)%n", statsUpdateTimestamp,
				timeSecondsElapsed));
		a.a("Most requested sections: ");
		Iterator<SectionHitsDto> i = sectionHitsDtoList.iterator();
		while (i.hasNext()) {
			SectionHitsDto shDto = i.next();
			a.a(String.format("<%s> (%d hits)", shDto.getSection(), shDto.getHits()));
			if (i.hasNext()) {
				a.a(" | ");
			}
			else {
				a.a("\n");
			}
		}
		a.a(String.format("total site hits <%d> | topTraffic <%s> (%d)%n", dto.getTotalHits(), dto.getTopTrafficIp(),
				dto.getTopTraffic()));
		a.a(String.format("topSc200 <%s> (%d) | topSc302 <%s> (%d) | ", dto.getTopSc200Ip(), dto.getTopSc200Count(),
				dto.getTopSc302Ip(), dto.getTopSc302Count()));
		a.a(String.format("topSc5xx <%s> (%d) | topSc4xx <%s> (%d)%n", dto.getTopSc5xxIp(), dto.getTopSc5xxCount(),
				dto.getTopSc4xxIp(), dto.getTopSc4xxCount()));
		a.a("Approximate request methods breakdown: ");
		Iterator<String> reqMethIterator = dto.getHttpMethodsObserved().iterator();
		while (reqMethIterator.hasNext()) {
			String method = reqMethIterator.next();
			a.a(String.format("%s : %.2f", method, dto.getHttpMethodFraction(method)));
			if (reqMethIterator.hasNext()) {
				a.a(" | ");
			}
			else {
				a.a("\n");
			}
		}
		a.a("'--");
		return a;
	}




	private Ansi buildAlarmListOutput(Ansi a) {
		if (alarmsHistory.size() > 0) {
			a.saveCursorPosition().cursor(0, 0).eraseLine();
			for (Alarm alrm : alarmsHistory) {
				if (alrm.type == Alarm.TYPE_RAISED) {
					a.fgBrightRed();
				}
				else {
					a.fgBrightGreen();
				}
				a.a(alrm.message + "\n");
			}
		}
		return a;
	}




	public void resetAlarm(long timestamp, int totalTraffic, int alarmThreshold, double trafficToThresholdRatio) {
		String strTimestamp = Formatting.CLF_DF.format(new Date(timestamp));
		String message = String.format("(i) [%s] traffic (%d) has fallen back within %.1f%% of threshold", strTimestamp,
				totalTraffic, trafficToThresholdRatio * 100);
		Alarm alarm = new Alarm(timestamp, Alarm.TYPE_RESET, message);
		alarmsHistory.addLast(alarm);
	}




	public void raiseAlarm(long timestamp, int totalTraffic, int alarmThreshold, double trafficToThresholdRatio) {
		String strTimestamp = Formatting.CLF_DF.format(new Date(timestamp));
		String message = String.format("<!> [%s] ALARM: traffic crossed the threshold (%d > %d) %.1f%% above ",
				strTimestamp, totalTraffic, alarmThreshold, (trafficToThresholdRatio - 1) * 100);
		Alarm alarm = new Alarm(timestamp, Alarm.TYPE_RAISED, message);
		alarmsHistory.addLast(alarm);
	}




	public void updateAlarm(long timestamp, int totalTraffic, int alarmThreshold, double trafficToThresholdRatio) {
		String strAlarmUpdateTs = Formatting.CLF_DF.format(new Date(timestamp));
		Alarm lastAlarm = alarmsHistory.removeLast();
		String strLastAlarmCreationTs = Formatting.CLF_DF.format(new Date(lastAlarm.ts));
		String fmtUpdatedAlarmMessage = "<!> [%s] ALARM UPDATE (%s): (%d > %d) total traffic is now  %.1f%% above threshold";
		String updatedAlarmMessage = String.format(fmtUpdatedAlarmMessage, strLastAlarmCreationTs, strAlarmUpdateTs,
				totalTraffic, alarmThreshold, (trafficToThresholdRatio - 1) * 100);
		Alarm updatedAlarm = new Alarm(lastAlarm.ts, Alarm.TYPE_RAISED, updatedAlarmMessage);
		alarmsHistory.addLast(updatedAlarm);
	}




	public void closeConsole() {
		AnsiConsole.out().print(Ansi.ansi().reset().fg(Ansi.Color.WHITE));
		AnsiConsole.systemUninstall();
	}
}
