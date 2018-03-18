package org.gmnz.hamon.integration;


import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.gmnz.hamon.parsing.LogLineDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class DaoTest {

	private ServerEngine serverEngine;
	private Dao dao;

	static {
		BasicConfigurator.configure();
	}




	@Before
	public void setup() {
		serverEngine = new ServerEngine();
		serverEngine.start();
		dao = new Dao(serverEngine);
	}




	@Test
	public void daoInit() throws SQLException {
		dao.init();
	}




	@Test(expected = DaoException.class)
	public void persist() throws SQLException, DaoException {
		dao.init();
		dao.persist(new LogLineDto());
	}




	@Test
	public void getTopSections() throws SQLException, DaoException {
		dao.init();
		Assert.assertEquals(0, dao.getTopSections().size());
	}




	@After
	public void tearDown() {
		serverEngine.stop();
	}

}
