package org.gmnz.hamon.integration;


import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * creato da simone in data 12/03/2018.
 */
public class ServerEngine {

	private Server server;


	private Connection connection;




	public ServerEngine() {
		server = new Server();
		HsqlProperties properties = new HsqlProperties();
		properties.setProperty("server.database.0", "file:hamonDb");
		properties.setProperty("server.dbname.0", "hamon");
		properties.setProperty("server.no_system_exit", "true");
//		properties.setProperty("server.silent", "false");
//		properties.setProperty("server.trace", "true");

		try {
			server.setProperties(properties);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ServerAcl.AclFormatException e) {
			e.printStackTrace();
		}
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}


	}




	public void start() {
		server.start();
		try {
			// added the "shutdown=true" connection option so that the server is
			// automatically shut down when the last connection is closed
			// - as we mantain a single connection throughout the application
			String jdbcUrl = "jdbc:hsqldb:hsql://localhost:9001/hamon;shutdown=true";
			connection = DriverManager.getConnection(jdbcUrl, "SA", "");
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}




	public Connection getConnection() {
		return connection;
	}




	public void stop() {
		try {
			Statement s = connection.createStatement();
			s.execute("SHUTDOWN");
			s.close();
			connection.close();
		}
		catch (SQLException e) {
			System.err.println("WARNING - shutdown command was not correcly executed");
			e.printStackTrace();
		}
		server.shutdown();
	}


}
