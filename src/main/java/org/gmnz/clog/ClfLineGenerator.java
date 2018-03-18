package org.gmnz.clog;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.gmnz.util.Formatting;


public class ClfLineGenerator {

	private final String[] ipV4Pool = {"205.143.105.218", "204.69.81.18", "217.129.241.133", "105.64.134.107",
			"105.185.10.163", "123.152.129.6", "254.96.225.18", "225.174.43.67", "177.223.136.121", "210.169.182.50",
			"102.51.92.249", "127.94.197.64", "129.140.150.22", "42.23.128.99", "158.118.239.197", "177.61.33.236",
			"59.247.85.103", "227.28.164.160", "254.94.33.22", "192.54.181.151"};

	private final String[] usernamePool = {"james", "woodrow", "warren", "herbert", "calvin", "thomas", "chester",
			"lyndon", "zachary", "jimmy", "harry"};

	

	private final String[] httpMethods = {"PUT", "HEAD", "GET", "POST", "DELETE", "OPTIONS"};

	private final String[] pathComponents = {"Helena", "Frankfort", "OklahomaCity", "Sacramento", "Annapolis", "Salem",
			"Honolulu", "Pierre", "Boise", "Juneau", "Augusta", "Augusta", "Cheyenne", "Nashville", "Trenton", "Helena",
			"Columbus", "Richmond", "LittleRock", "Nashville", "Phoenix", "Jackson", "Pierre", "Boston", "Annapolis",
			"Raleigh", "DesMoines", "Providence", "Helena", "Concord", "Lansing", "Cheyenne", "Charleston", "Honolulu",
			"Boise", "Pierre", "Sacramento", "Dover", "Tallahassee", "Nashville", "LittleRock", "CarsonCity", "Columbia",
			"Madison", "Trenton", "SantaFe", "Montpelier", "Albany", "Providence", "Atlanta"};

	//@formatter:off
	private final String[] goldSaints = {
			"Mu", "Aldebaran", "Saga", "", "Deathmask",
			"Aiolia", "Shaka", "Dohko", "Milo",
			"Aiolos", "Shura", "Camus", "Aphrodite"};
	//@formatter:on

	private final String[] extensions = {"html", "gif", "png", "js", "css", "cgi"};

	private final int[] httpStatusCodes = {302, 200, 401, 404, 500};

	private final Random rnd;





	private enum FlipCoin {
		heads, tails
	}




	public ClfLineGenerator() {
		rnd = new Random();
	}




	private FlipCoin flipCoin() {
		return rnd.nextDouble() > .5 ? FlipCoin.heads : FlipCoin.tails;
	}




	private String generateRandomIpV4Address() {
		return ipV4Pool[rnd.nextInt(ipV4Pool.length)];
	}




	private String generateSiteUrl(String httpMethod) {
		StringBuilder siteUrl = new StringBuilder("/");

		int sectionNameIndex = Math.abs(
				(int) Math.round(goldSaints.length / 2 + goldSaints.length / 2 * rnd.nextGaussian())) % goldSaints.length;
		siteUrl.append(goldSaints[sectionNameIndex]);

		int depth = rnd.nextInt(6);

		for (int i = 1; i < depth; i++) {
			siteUrl.append("/").append(pathComponents[rnd.nextInt(pathComponents.length)]);
		}

		if (httpMethod.equalsIgnoreCase("get")) {
			if (flipCoin() == FlipCoin.heads) {
				siteUrl.append("?param" + rnd.nextInt(10) + "=" + UUID.randomUUID().toString());
			} else {
				siteUrl.append("." + extensions[rnd.nextInt(extensions.length)]);
			}
		}

		return siteUrl.toString();
	}




	private String generateRequestLogLine() {
		StringBuilder sb = new StringBuilder("\"");
		// metodo http
		int methodIndex = Math.abs((int) Math.round(2.5 + 2 * rnd.nextGaussian())) % httpMethods.length;
		String method = httpMethods[methodIndex];
		sb.append(method).append(" ");

		sb.append(generateSiteUrl(method));

		sb.append(" HTTP/1.1\"");
		return sb.toString();
	}




	public String generateClfLine() {
		StringBuilder sbLogLine = new StringBuilder();

		// ip
		sbLogLine.append(generateRandomIpV4Address()).append(" ");

		// remote id
		if (flipCoin() == FlipCoin.heads) {
			sbLogLine.append(usernamePool[rnd.nextInt(usernamePool.length)]).append(" ");
		} else {
			sbLogLine.append("- ");
		}

		// auth id
		if (flipCoin() == FlipCoin.heads) {
			sbLogLine.append(usernamePool[rnd.nextInt(usernamePool.length)]).append(" ");
		} else {
			sbLogLine.append("- ");
		}

		// timestamp
		sbLogLine.append("[" + Formatting.CLF_DF.format(new Date()) + "] ");

		// request
		sbLogLine.append(generateRequestLogLine()).append(" ");

		// status code
		/*
		It'd be great to simulate a normal distribution of status codes "concentrated" at around 200.
		Too bad the index corresponding to the status code 500 is 4: this means it falls quite outside
		the region with reasonable probability (sigma is 1). Means we hardly get status codes 500.

		Let's try cheating a bit with the mean value of the gaussian curve
		 */
//		int statusCodeIndex = (int) Math.round(Math.abs(rnd.nextGaussian()));
		int statusCodeIndex = 1 + (int) Math.round(rnd.nextGaussian());
		// out of bounds is for golf players
		if (statusCodeIndex >= httpStatusCodes.length) {
			statusCodeIndex = httpStatusCodes.length - 1;
		} else if (statusCodeIndex < 0) {
			statusCodeIndex = 0;
		}
		int httpStatusCode = httpStatusCodes[statusCodeIndex];
		sbLogLine.append(httpStatusCode).append(" ");

		// bytes tx
		if (httpStatusCode == 302) {
			sbLogLine.append("-");
		} else {
			sbLogLine.append(Math.round(20000 + rnd.nextGaussian() * 4000));
		}

		return sbLogLine.toString();
	}




	public List<String> generateClfLines(int nLines) {
//		System.out.println(Thread.currentThread().getName() + " - generating " + nLines + " lines of log");
		List<String> logLines = new ArrayList<>(nLines);
		for (int i = 0; i < nLines; i++) {
			logLines.add(generateClfLine());
		}
		return logLines;
	}
}
