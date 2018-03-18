package org.gmnz.clog;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gmnz.util.ServerSocketListener;
import org.gmnz.util.ServerSocketTask;
import org.gmnz.util.SocketUtil;


public class Main implements LogLineGeneratorListener, ServerSocketListener {


	private GeneratorTask logLineGeneratorTask;
	private ServerSocketTask serverSocketTask;
	private PrintWriter pw;

	private ExecutorService executor;

	private static final String HOSTNAME = "localhost";
	private static final int PORT = 19756;

	private static final Logger log = Logger.getLogger(Main.class);

	static {
		BasicConfigurator.configure();
	}




	private Main(String targetFileName) throws IOException {
		log.info(String.format("Writing CLF random log file to <%s>%n", targetFileName));
		logLineGeneratorTask = new GeneratorTask(new ClfLineGenerator(), this);
		serverSocketTask = new ServerSocketTask(HOSTNAME, PORT, this);
		pw = new PrintWriter(new FileWriter(targetFileName, false));
	}




	private Main() {}




	private void execute() {
		executor = Executors.newCachedThreadPool();
		executor.execute(logLineGeneratorTask);
		executor.execute(serverSocketTask);
		executor.shutdown();
	}




	@Override
	public void serverShutdownRequested() {
		log.info("shutdown requested");
		logLineGeneratorTask.stop();
		log.info("log generation task has been signaled to stop");
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		pw.flush();
		pw.close();
		log.info("output writer flushed and closed");
	}




	@Override
	public void logLinesGenerated(List<String> logLines) {
		for (String logLine : logLines) {
			pw.println(logLine);
		}
		pw.flush();
	}




	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println(
					"Wrong number of arguments, either run with no arguments for plain log generation or with \"-shutdown\" argment to stop a running instance");
			System.exit(2);
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("-shutdown")) {
				SocketUtil.executeOrder(Main.HOSTNAME, Main.PORT, ORDER_66);
			}
			else {
				Main m = new Main(args[0]);
				m.execute();
			}
		}
	}

}
