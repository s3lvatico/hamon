package org.gmnz.clog;


import java.util.List;
import java.util.concurrent.TimeUnit;


class GeneratorTask implements Runnable {

	private final ClfLineGenerator generator;
	private final LogLineGeneratorListener listener;

	private static final long SAMP_PERIOD_MS = 1000;
	private static final double DUTY_CYCLE = .35;
	private static final double GENERATION_COUNT = 180;

	private static final int LOW_TRAFFIC = 2;
	private static final int HIGH_TRAFFIC = 15;

	private boolean active = true;




	GeneratorTask(ClfLineGenerator generator, LogLineGeneratorListener listener) {
		this.generator = generator;
		this.listener = listener;
		active = true;
	}




	void stop() {
		active = false;
	}




	@Override
	public void run() {
		double cursor = 1;
		int trafficLoad;
		while (active) {
			sleep();
			double positionInDutyCycle = cursor / GENERATION_COUNT;
			if (positionInDutyCycle < DUTY_CYCLE) {
				trafficLoad = LOW_TRAFFIC;
			} else {
				trafficLoad = HIGH_TRAFFIC;
			}
			List<String> logLines = generator.generateClfLines(trafficLoad);
			listener.logLinesGenerated(logLines);
			cursor = cursor + 1;
			if (cursor > GENERATION_COUNT) {
				cursor = 1;
			}
		}
	}




	private void sleep() {
		Thread.yield();
		try {
			TimeUnit.MILLISECONDS.sleep(SAMP_PERIOD_MS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
