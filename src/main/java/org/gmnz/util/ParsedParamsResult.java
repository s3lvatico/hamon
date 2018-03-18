package org.gmnz.util;


import java.util.HashSet;
import java.util.Set;



public class ParsedParamsResult {

	public Set<String> errorMessages = new HashSet<>();

	public String targetFileName = "";

	public int alarmThreshold = -1;

	public HamonCommand command = HamonCommand.NONE;




	void validate() {
		switch (command) {
		case SHUTDOWN:
			break;
		case EXECUTION:
			if (targetFileName.isEmpty()) {
				errorMessages.add("no target file name specified");
			}
			if (alarmThreshold < 0) {
				errorMessages.add("invalid value for alarm threshold");
			}
			break;
		case NONE:
			break;
		default:
			break;
		}
	}




	public boolean paramsValid() {
		switch (command) {
		case SHUTDOWN:
			return true;
		case EXECUTION:
			return errorMessages.isEmpty() && !targetFileName.isEmpty() && alarmThreshold >= 0;
		case NONE:
			errorMessages.add("undefined command status");
			return false;
		}
		return false;
	}

}

