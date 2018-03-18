package org.gmnz.util;


public class ParamsValidator {

	public ParsedParamsResult checkParams(String[] args) {

		ParsedParamsResult ppr = new ParsedParamsResult();

		if (args.length == 1 || args.length == 4) {

			switch (args.length) {
				case 1:
					if (args[0].equals("-shutdown")) {
						ppr.command = HamonCommand.SHUTDOWN;
					} else {
						ppr.errorMessages.add("invalid parameter specified");
					}
					break;
				case 4:
					ppr.command = HamonCommand.EXECUTION;
					int i = 0;
					try {
						while (i < args.length) {
							if (args[i].equals("-f")) {
								ppr.targetFileName = args[++i];
							}
							if (args[i].equals("-t")) {
								try {
									ppr.alarmThreshold = Integer.parseInt(args[++i]);
								} catch (NumberFormatException nfe) {
									ppr.errorMessages.add(nfe.getMessage());
								}
							}
							i++;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
						ppr.errorMessages.add("wrong number of parameters");
					}
					break;
			}
		} else {
			ppr.errorMessages.add("wrong number of parameters");
		}
		ppr.validate();
		return ppr;
	}

}
