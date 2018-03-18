package org.gmnz.hamon.parsing;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


class Sampler implements Runnable {

	private final Charset charset;

	private boolean active;
	private Path targetFilePath;
	private CommonLogParser logParser;

	private static final Logger log = Logger.getLogger(Sampler.class);




	Sampler(String targetFileName, CommonLogParser logParser) {
		charset = Charset.forName("UTF-8");
		active = true;
		targetFilePath = Paths.get(targetFileName);
		this.logParser = logParser;
	}




	@Override
	public void run() {
		try (BufferedReader reader = Files.newBufferedReader(targetFilePath, charset)) {
			String line = null;
			List<String> batch;
			while (active) {
				batch = new ArrayList<>();
				while ((line = reader.readLine()) != null) {
					batch.add(line);
				}
				// no need to bother the parser if we don't have data
				if (batch.size() != 0) {
					log.debug("clf lines to process: " + batch.size());
					logParser.processBatch(batch);
				}
				Thread.yield();
				TimeUnit.SECONDS.sleep(5);
			}
		}
		catch (IOException e) {
			log.error("IOException: %s%n", e);
		}
		catch (InterruptedException e) {
			log.warn("interrupted", e);
		}
	}




	void activate() {
		active = true;
	}




	void deactivate() {
		active = false;
	}
}
