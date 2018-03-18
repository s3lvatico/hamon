package org.gmnz.hamon;


import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.gmnz.hamon.analysis.Analyzer;
import org.gmnz.hamon.integration.Dao;
import org.gmnz.hamon.integration.ServerEngine;
import org.gmnz.hamon.parsing.CommonLogParser;
import org.gmnz.util.ParamsValidator;
import org.gmnz.util.ParsedParamsResult;
import org.gmnz.util.ServerSocketListener;
import org.gmnz.util.ServerSocketTask;
import org.gmnz.util.SocketUtil;


public class Main implements ServerSocketListener {

	private ServerEngine serverEngine;
	private CommonLogParser logParser;
	private Analyzer analyzer;
	private Dao dao;

	private ServerSocketTask serverSocketTask;

	private static final String HOSTNAME = "localhost";
	private static final int PORT = 19757;

	private final ExecutorService executor;

	private static final Logger log = Logger.getLogger(Main.class);




	private Main(String targetLogFileName, int alarmThreshold) {
		log.info("starting in EXECUTION mode");
		serverEngine = new ServerEngine();
		dao = new Dao(serverEngine);
		logParser = new CommonLogParser(targetLogFileName, dao);
		analyzer = new Analyzer(dao, alarmThreshold);

		serverSocketTask = new ServerSocketTask(HOSTNAME, PORT, this);
		executor = Executors.newSingleThreadExecutor();
	}




	private void start() throws SQLException {
		serverEngine.start();
		log.info("server engine started");

		dao.init();
		log.info("dao initialized");

		logParser.start();
		log.info("logParser started");

		analyzer.start();
		log.info("analyzer started");

		executor.execute(serverSocketTask);
		executor.shutdown();

	}




	@Override
	public void serverShutdownRequested() {
		log.info("stopping analyzer");
		analyzer.stop();
		log.info("stopping logParser");
		logParser.stop();
		log.info("stopping the server engine");
		serverEngine.stop();
	}




	private static void showUsage() {
		System.err.println("Accepted parameters are: -t <trafficThreshold> -f <targetLogFileName>");
	}




	public static void main(String[] args) throws IOException {
		ParsedParamsResult ppr = new ParamsValidator().checkParams(args);

		if (!ppr.paramsValid()) {
			log.error(ppr.errorMessages);
			System.err.println(ppr.errorMessages);
			showUsage();
		}
		else {
			switch (ppr.command) {
			case EXECUTION:
				Main m = new Main(ppr.targetFileName, ppr.alarmThreshold);
				try {
					m.start();
				}
				catch (SQLException e) {
					log.error("Exception during initialization process", e);
					e.printStackTrace();
					System.exit(1);
				}
				break;
			case SHUTDOWN:
				SocketUtil.executeOrder(Main.HOSTNAME, Main.PORT, ORDER_66);
				break;
			default:
				log.error("undef'd execution command!");
			}
		}
	}

}
