package org.gmnz.hamon.output;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;


public class Ja02 {

	void execute() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		PeriodicPrinter pp = new PeriodicPrinter();
		executorService.execute(pp);
		executorService.shutdown();
		try {
			Thread.sleep(10000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		pp.stop();
	}




	public static void main(String[] args) {
		int i = 0;
		while (i < args.length) {
			System.out.format("args[%d]=%s%n", i, args[i++]);
		}
		AnsiConsole.systemInstall();

		new Ja02().execute();

		AnsiConsole.out().print(Ansi.ansi().reset().fg(Ansi.Color.WHITE));
		AnsiConsole.systemUninstall();

	}

}
