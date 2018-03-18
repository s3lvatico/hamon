package org.gmnz.hamon.integration;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.gmnz.hamon.parsing.LogLineDto;
import org.gmnz.util.Formatting;


public class Dao {

	private ServerEngine serverEngine;

	private static final String SQL_INSERT = "INSERT INTO data_depot VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String SQL_TOP_SECTIONS = "SELECT SITE_SECTION, count(SITE_SECTION) hits FROM DATA_DEPOT GROUP BY SITE_SECTION ORDER BY hits DESC";

	private static final String SQL_TOP_TRAFFIC = "SELECT top 1 ip, traffic  FROM V_TRAFFIC_STATS WHERE TRAFFIC IS NOT NULL ORDER BY TRAFFIC DESC";
	private static final String SQL_TOP_200 = "SELECT top 1 ip, SUCCESSFUL_REQUESTS  FROM V_TRAFFIC_STATS WHERE SUCCESSFUL_REQUESTS IS NOT NULL  ORDER BY SUCCESSFUL_REQUESTS DESC";
	private static final String SQL_TOP_302 = "SELECT top 1 ip, REDIRECTS  FROM V_TRAFFIC_STATS WHERE REDIRECTS IS NOT NULL  ORDER BY REDIRECTS DESC";
	private static final String SQL_TOP_5xx = "SELECT top 1 ip, SERVER_ERROR_COUNT  FROM V_TRAFFIC_STATS WHERE SERVER_ERROR_COUNT IS NOT NULL  ORDER BY SERVER_ERROR_COUNT DESC";
	private static final String SQL_TOP_4xx = "SELECT top 1 ip, CLIENT_ERROR_COUNT  FROM V_TRAFFIC_STATS WHERE CLIENT_ERROR_COUNT IS NOT NULL  ORDER BY CLIENT_ERROR_COUNT DESC";
	private static final String SQL_METHODS = "SELECT HTTP_REQ_METHOD, COUNT(HTTP_REQ_METHOD) FROM DATA_DEPOT GROUP BY HTTP_REQ_METHOD";
	private static final String SQL_TOTAL_HITS = "SELECT COUNT(1) FROM DATA_DEPOT";

	private static final String SQL_TRUNCATE = "TRUNCATE TABLE public.data_depot";



	private static final String SQL_FMT_TRAFFIC_TIME_WINDOW = "SELECT SUM(BYTES_TX) FROM DATA_DEPOT WHERE TS BETWEEN DATE_SUB(TIMESTAMP '%s', INTERVAL %d MINUTE) AND TIMESTAMP '%s'";

/*
 * SELECT SUM(BYTES_TX) FROM DATA_DEPOT WHERE TS BETWEEN DATE_SUB(TIMESTAMP
 * '2018-03-07 08:35:54', INTERVAL 3 MINUTE) AND TIMESTAMP '2018-03-07
 * 08:35:54';
 */

	private static final String SQL_CREATE_TBL = "CREATE MEMORY TABLE IF NOT EXISTS PUBLIC.DATA_DEPOT(TS TIMESTAMP NOT NULL,SOURCE_IP VARCHAR(40) NOT NULL,HTTP_REQ_METHOD VARCHAR(8) NOT NULL,REQ_URL VARCHAR(4096) NOT NULL,SITE_SECTION VARCHAR(100),RESPONSE_CODE INTEGER NOT NULL,BYTES_TX INTEGER)";

	private static final String SQL_CREATE_VIEW = "CREATE VIEW IF NOT EXISTS PUBLIC.V_TRAFFIC_STATS (IP,TRAFFIC,SUCCESSFUL_REQUESTS,REDIRECTS,SERVER_ERROR_COUNT,CLIENT_ERROR_COUNT) AS SELECT SUBQ_TRAFFIC.IP,SUBQ_TRAFFIC.TRAFFIC,SUBQ_REQOK.SUCCESSFUL_REQUESTS,SUBQ_REDIRECTS.REDIRECTS,SUBQ_SRVERROR.SERVER_ERROR_COUNT,SUBQ_CLTERROR.CLIENT_ERROR_COUNT FROM(SELECT SOURCE_IP IP,SUM(BYTES_TX)AS TRAFFIC FROM PUBLIC.DATA_DEPOT GROUP BY SOURCE_IP)SUBQ_TRAFFIC LEFT JOIN(SELECT SOURCE_IP IP,COUNT(RESPONSE_CODE)AS SUCCESSFUL_REQUESTS FROM PUBLIC.DATA_DEPOT WHERE RESPONSE_CODE BETWEEN 200 AND 299 GROUP BY SOURCE_IP)SUBQ_REQOK ON SUBQ_TRAFFIC.IP=SUBQ_REQOK.IP LEFT JOIN(SELECT SOURCE_IP IP,COUNT(RESPONSE_CODE)AS REDIRECTS FROM PUBLIC.DATA_DEPOT WHERE RESPONSE_CODE=302 GROUP BY SOURCE_IP)SUBQ_REDIRECTS ON SUBQ_REDIRECTS.IP=SUBQ_TRAFFIC.IP LEFT JOIN(SELECT SOURCE_IP IP,COUNT(RESPONSE_CODE)AS SERVER_ERROR_COUNT FROM PUBLIC.DATA_DEPOT WHERE RESPONSE_CODE BETWEEN 500 AND 599 GROUP BY SOURCE_IP)SUBQ_SRVERROR ON SUBQ_SRVERROR.IP=SUBQ_TRAFFIC.IP LEFT JOIN(SELECT SOURCE_IP IP,COUNT(RESPONSE_CODE)AS CLIENT_ERROR_COUNT FROM PUBLIC.DATA_DEPOT WHERE RESPONSE_CODE BETWEEN 400 AND 499 GROUP BY SOURCE_IP)SUBQ_CLTERROR ON SUBQ_CLTERROR.IP=SUBQ_TRAFFIC.IP";

	private static final Logger log = Logger.getLogger(Dao.class);




	public Dao(ServerEngine serverEngine) {
		this.serverEngine = serverEngine;
	}




	public void init() throws SQLException {
		Statement query = null;
		try {
			query = serverEngine.getConnection().createStatement();
			checkSchema(query);
			query.execute(SQL_TRUNCATE);
		}
		//@formatter:off
		finally { if (query != null) try { query.close(); } catch (SQLException e) {} }
		//@formatter:on

	}




	private void checkSchema(Statement s) throws SQLException {
		s.execute(SQL_CREATE_TBL);
		s.execute(SQL_CREATE_VIEW);
	}




	private void cleanUp(Statement s, ResultSet rs) {
		//@formatter:off
		if (rs != null) { try { rs.close(); } catch (SQLException e) { /* no one cares now */ } }
		if (s != null) { try { s.close(); } catch (SQLException e) { /* no one cares now */ } }
		//@formatter:on
	}




	public void persist(LogLineDto logLineDto) throws DaoException {
		PreparedStatement insert = null;
		try {
			insert = serverEngine.getConnection().prepareStatement(SQL_INSERT);
			insert.setTimestamp(1, new Timestamp(logLineDto.timestamp.getTime()));
			insert.setString(2, logLineDto.ipAddress);
			insert.setString(3, logLineDto.httpMethod);
			insert.setString(4, logLineDto.requestUrl);
			if (logLineDto.section != null) {
				insert.setString(5, logLineDto.section);
			}
			else {
				insert.setNull(5, Types.VARCHAR);
			}
			insert.setInt(6, logLineDto.httpStatusCode);
			if (logLineDto.bytesTransferred != null) {
				insert.setInt(7, logLineDto.bytesTransferred);
			}
			else {
				insert.setNull(7, Types.INTEGER);
			}

			insert.execute();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			log.warn("could not persist the log line " + logLineDto, e);
			throw new DaoException("could not persist the log line " + logLineDto, e);
		}
		finally {
			cleanUp(insert, null);
		}
	}




	public List<SectionHitsDto> getTopSections() throws DaoException {
		Statement query = null;
		ResultSet rs = null;
		List<SectionHitsDto> resultList = new ArrayList<>();
		try {
			query = serverEngine.getConnection().createStatement();
			rs = query.executeQuery(SQL_TOP_SECTIONS);
			while (rs.next()) {
				resultList.add(new SectionHitsDto(rs.getString(1), rs.getInt(2)));
			}
		}
		catch (SQLException e) {
			throw new DaoException("unable to get the top section list from the database", e);
		}
		finally {
			cleanUp(query, rs);
		}
		return resultList;
	}




	public GeneralInfoDto getGeneralStats() throws DaoException {
		Statement query = null;
		ResultSet rs = null;

		String topTrafficIp = "";
		int topTraffic = -1;
		String topSc200Ip = "";
		int topSc200Count = -1;
		String topSc302Ip = "";
		int topSc302Count = -1;
		String topSc5xxIp = "";
		int topSc5xxCount = -1;
		String topSc4xxIp = "";
		int topSc4xxCount = -1;

		int totalHits = -1;

		GeneralInfoDto generalInfoDto = null;
		try {
			query = serverEngine.getConnection().createStatement();

			rs = query.executeQuery(SQL_TOP_TRAFFIC);
			while (rs.next()) {
				topTrafficIp = rs.getString(1);
				topTraffic = rs.getInt(2);
			}

			rs.close();
			rs = query.executeQuery(SQL_TOP_200);
			while (rs.next()) {
				topSc200Ip = rs.getString(1);
				topSc200Count = rs.getInt(2);
			}

			rs.close();
			rs = query.executeQuery(SQL_TOP_302);
			while (rs.next()) {
				topSc302Ip = rs.getString(1);
				topSc302Count = rs.getInt(2);
			}

			rs.close();
			rs = query.executeQuery(SQL_TOP_5xx);
			while (rs.next()) {
				topSc5xxIp = rs.getString(1);
				topSc5xxCount = rs.getInt(2);
			}

			rs.close();
			rs = query.executeQuery(SQL_TOP_4xx);
			while (rs.next()) {
				topSc4xxIp = rs.getString(1);
				topSc4xxCount = rs.getInt(2);
			}

			rs.close();
			rs = query.executeQuery(SQL_TOTAL_HITS);
			while (rs.next()) {
				totalHits = rs.getInt(1);
			}
			generalInfoDto = new GeneralInfoDto(topTrafficIp, topTraffic, topSc200Ip, topSc200Count, topSc302Ip,
					topSc302Count, topSc5xxIp, topSc5xxCount, topSc4xxIp, topSc4xxCount, totalHits);

			rs.close();
			rs = query.executeQuery(SQL_METHODS);
			while (rs.next()) {
				double methodFraction = ((double) rs.getInt(2)) / totalHits;
				generalInfoDto.addHttpMethodFraction(rs.getString(1), methodFraction);
			}
		}
		catch (SQLException e) {
			throw new DaoException("could not obtain general statistics from the db", e);
		}
		finally {
			cleanUp(query, rs);
		}

		return generalInfoDto;
	}




	public int getTotalTrafficInTimeWindow(int windowWidth) {
		Statement query = null;
		ResultSet rs = null;
		String tsNow = Formatting.DB_DF.format(new Date());
		String sqlTrafficTimeWindow = String.format(SQL_FMT_TRAFFIC_TIME_WINDOW, tsNow, windowWidth, tsNow);
		int totalTraffic = -1;
		try {
			query = serverEngine.getConnection().createStatement();
			rs = query.executeQuery(sqlTrafficTimeWindow);
			while (rs.next()) {
				totalTraffic = rs.getInt(1);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			cleanUp(query, rs);
		}
		return totalTraffic;
	}

}
