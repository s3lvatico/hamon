package org.gmnz.hamon.analysis;


import org.apache.log4j.Logger;
import org.gmnz.hamon.integration.Dao;
import org.gmnz.hamon.integration.DaoException;
import org.gmnz.hamon.integration.GeneralInfoDto;
import org.gmnz.hamon.integration.SectionHitsDto;

import java.util.List;
import java.util.concurrent.TimeUnit;


class InfoGatherer implements Runnable {

	long samplingPeriod;
	Dao dao;
	boolean active;
	InfoGathererListener listener;

	private static final Logger intgLog = Logger.getLogger(InfoGatherer.class);
	private static final Logger mainLog = Logger.getLogger("org.gmnz.hamon");




	@Override
	public void run() {
		while (active) {
			rest();
			List<SectionHitsDto> topSections = null;
			try {
				topSections = dao.getTopSections();
			}
			catch (DaoException e) {
				intgLog.warn("unable to get the top section list from the database", e);
			}
			GeneralInfoDto generalInfo = null;
			try {
				generalInfo = dao.getGeneralStats();
			}
			catch (DaoException e) {
				intgLog.warn("could not obtain general statistics from the db", e);
			}
			if (topSections != null && generalInfo != null) {
				listener.receiveTrafficStats(topSections, generalInfo);
			}
			else {
				mainLog.warn("unable to gather either general information or top sections hits - check integration logs");
			}
		}
	}




	private void rest() {
		Thread.yield();
		try {
			TimeUnit.SECONDS.sleep(samplingPeriod);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
