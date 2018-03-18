package org.gmnz.hamon.output;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

class PeriodicPrinter implements Runnable {

	private boolean enabled = true;

	private static final long SLEEP_TIME_MS = 500;

	@Override
	public void run() {
		int count = 0;
		while (enabled) {
			System.out.println(UUID.randomUUID().toString());
			Ansi a = Ansi.ansi().saveCursorPosition().cursor(0, 0).eraseLine();
			a.fgBrightRed().a(++count + " invocations requested");
			a.fgDefault().restoreCursorPosition();
			AnsiConsole.out().print(a);
			Thread.yield();
			try {
				TimeUnit.MILLISECONDS.sleep(SLEEP_TIME_MS);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	void stop() {
		enabled = false;
	}

}
