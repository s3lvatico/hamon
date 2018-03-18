package org.gmnz.hamon.integration;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ServerEngineTest {

	private ServerEngine serverEngine;




	@Before
	public void setup() {
		serverEngine = new ServerEngine();
		serverEngine.start();
	}




	@Test
	public void getConnection() throws SQLException {
		Connection c = serverEngine.getConnection();
		Statement s = c.createStatement();
		boolean queryWasExecuted = s.execute("SELECT count(1) FROM INFORMATION_SCHEMA.SCHEMATA");
		Assert.assertTrue(queryWasExecuted);
	}




	@After
	public void tearDown() {
		serverEngine.stop();
	}
}
