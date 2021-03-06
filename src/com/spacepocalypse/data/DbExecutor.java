package com.spacepocalypse.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class DbExecutor {
	private Logger log4jLogger;
	protected Connection dbConnection;
	
	public DbExecutor() {
		log4jLogger = Logger.getLogger(DbExecutor.class);
	}

	public void setDbConnection(Connection dbConnection) {
		this.dbConnection = dbConnection;
	}

	public Connection getDbConnection() {
		if (dbConnection == null) {
			// This will load the MySQL driver, each DB has its own driver
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				log4jLogger.error(e.getMessage());
				e.printStackTrace();
			}
			
			// Setup the connection with the DB
			try {
				dbConnection = DriverManager.getConnection(
						"jdbc:mysql://localhost/beerdb?" +
						"user=root&password=password");
			} catch (SQLException e) {
				log4jLogger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return dbConnection;
	}
	
	public void closeAndReconnect() {
		synchronized (dbConnection) {

			Level origLvl = log4jLogger.getLevel();
			log4jLogger.setLevel(Level.INFO);
			if (dbConnection != null) {
				log4jLogger.info("Attempting to close existing DB connection");
				try {
					dbConnection.close();
				} catch (SQLException e) {
					log4jLogger.error(e.getMessage());
					e.printStackTrace();
				}
			}
			log4jLogger.info("Nulling existing DB connection reference.");
			dbConnection = null;

			log4jLogger.info("Reconnecting.");
			getDbConnection();

			log4jLogger.setLevel(origLvl);
		}
	}
	
	public static void main(String[] args) {
		DbExecutor dbe = new DbExecutor();
		try {
			PreparedStatement ps = dbe.getDbConnection().prepareStatement("select count(*) from beers");
			ps.execute();
			ResultSet results = ps.getResultSet();
			while (results.next()) {
				System.out.println("RESULT: " + results.getInt(1));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				dbe.getDbConnection().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		
	}

}
