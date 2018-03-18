package org.gmnz.hamon.parsing;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gmnz.hamon.integration.Dao;
import org.gmnz.hamon.integration.DaoException;
import org.gmnz.util.Formatting;


public class CommonLogParser {

	private Dao dao;

	private static final String PARSE_REGEXP = "(?<ipAddress>.*?) (?<rfc1413id>.*?) (?<userId>.*?) \\[(?<timestamp>.*?)\\] "
			+ "\"(?<httpMethod>.*?) (?<requestUrl>.*?) HTTP/.*?\" (?<httpStatusCode>[0-9]*?) (?<bytesTransferred>.*?)$";


	private static final String SECTION_REGEXP = "/([^/]*)";


	private final Pattern ptnLogLine;
	private final Pattern ptnSection;
	private final Sampler sampler;


	private static final Logger log = Logger.getLogger(CommonLogParser.class);




	public CommonLogParser(String targetFileName, Dao dao) {
		ptnLogLine = Pattern.compile(PARSE_REGEXP);
		ptnSection = Pattern.compile(SECTION_REGEXP);
		this.dao = dao;
		sampler = new Sampler(targetFileName, this);
	}




	@Deprecated
	public void execute() throws DaoException {
		List<LogLineDto> parsedLogLines = processLog();
		System.out.format("Log lines successfully parsed: %d%n", parsedLogLines.size());
		for (LogLineDto dto : parsedLogLines) {
			dao.persist(dto);
		}
	}




	public void start() {
		ExecutorService executor = Executors.newCachedThreadPool();
		sampler.activate();
		executor.execute(sampler);
		executor.shutdown();
	}




	public void stop() {
		sampler.deactivate();
	}




	public void processBatch(List<String> logLines) {
		LogLineDto dto;
		for (String logLine : logLines) {
			dto = parseLogLine(logLine);
			if (dto != null) {
				try {
					dao.persist(dto);
				}
				catch (DaoException e) {
					log.warn("Could not persist parsed log line: " + dto, e);
				}
			}
		}
	}




	@Deprecated
	List<LogLineDto> processLog() {
		final InputStream resourceAsStream = CommonLogParser.class.getResourceAsStream("/log.txt");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
			List<LogLineDto> parsedLogLines = new ArrayList<>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				LogLineDto dto = parseLogLine(line);
				if (dto != null) {
					parsedLogLines.add(dto);
				}
				else {
					System.err.format("<%s> COULD NOT BE PARSED", line);
				}
			}
			// Thread.yield();
			// TimeUnit.SECONDS.sleep(5);
			return parsedLogLines;
		}
		catch (IOException x) {
			System.err.format("IOException: %s%n", x);
			return null;
		}
	}




	private LogLineDto parseLogLine(String logLine) {
		Matcher mLogLineTokens = ptnLogLine.matcher(logLine);
		boolean found = mLogLineTokens.find();
		Matcher mSection;
		if (found) {
			LogLineDto dto = new LogLineDto();
			dto.ipAddress = mLogLineTokens.group("ipAddress");
			dto.rfc1413id = mLogLineTokens.group("rfc1413id");
			dto.userId = mLogLineTokens.group("userId");

			dto.timestamp = parseTimestamp(mLogLineTokens.group("timestamp"));
			dto.httpMethod = mLogLineTokens.group("httpMethod");

			String requestUrl = mLogLineTokens.group("requestUrl");
			dto.requestUrl = requestUrl;
			mSection = ptnSection.matcher(requestUrl);
			mSection.find();
			dto.section = mSection.group(1).length() != 0 ? mSection.group(1) : "ROOT";
			dto.httpStatusCode = parseNumericValue(mLogLineTokens.group("httpStatusCode"));
			dto.bytesTransferred = parseNumericValue(mLogLineTokens.group("bytesTransferred"));
			return dto;
		}
		else {
			log.warn("unparseable log line: <" + logLine + ">");
			return null;
		}
	}




	private Date parseTimestamp(String timestamp) {
		try {
			return Formatting.CLF_DF.parse(timestamp);
		}
		catch (ParseException e) {
			// TODO setta flag di errore appropriata
			e.printStackTrace();
			return null;
		}
	}




	private Integer parseNumericValue(String s) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			// TODO setta flag di errore appropriata
			return null;
		}
	}



//	private Integer parseBytesTransferred(String bytesTransferred) {
//		try {
//			return Integer.parseInt(bytesTransferred);
//		} catch (NumberFormatException e) {
//			// TODO setta flag di errore appropriata
//			return null;
//		}
//	}

}
