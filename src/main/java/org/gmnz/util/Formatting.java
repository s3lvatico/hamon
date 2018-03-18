package org.gmnz.util;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class Formatting {

	public static final DateFormat CLF_DF = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

	public static final DateFormat DB_DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");




	public static String formatClfTimestamp(long ts) {
		return CLF_DF.format(new Date(ts));
	}

}
