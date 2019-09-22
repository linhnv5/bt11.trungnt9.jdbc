package com.topica.edu.itlab.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;

/**
 * Utility class to connect db
 * @author ljnk975
 */
public class MySQLConnection {

	// Singleton
	private static class MySQLConnectionHelper {
		public static MySQLConnection instance = new MySQLConnection();
	}

	private MySQLConnection() {
	}
	
	public static MySQLConnection gI() {
		return MySQLConnectionHelper.instance;
	}

	// Connection and statement
	private Connection connection;
	private Statement statement;

	/**
	 * Connect to database
	 * @param host   Host name
	 * @param dbName DataBase Name
	 * @param user   Username
	 * @param pass   Password of user
	 * @throws SQLException
	 * @throws SQLTimeoutException
	 * @throws ClassNotFoundException
	 */
	public void connect(String host, String dbName, String user, String pass) throws SQLException, SQLTimeoutException, ClassNotFoundException {
		connection = getMySQLConnection(host, dbName, user, pass);
		statement  = connection.createStatement();
	}

	/**
	 * Helper function to create connection to database
	 * @param host   Host name
	 * @param dbName DataBase Name
	 * @param user   Username
	 * @param pass   Password of user
	 * @return       Connection to database
	 * @throws SQLException
	 * @throws SQLTimeoutException
	 * @throws ClassNotFoundException
	 */
	private Connection getMySQLConnection(String host, String dbName, String user, String pass) throws SQLException, SQLTimeoutException, ClassNotFoundException {
		// Khai bao class driver cho jdbc
		Class.forName("com.mysql.cj.jdbc.Driver");
	 
		// cau truc url jdbc:mysql://localhost:3306/simplehr
		String connectionURL = "jdbc:mysql://" + host + ":3306/" + dbName;

		return DriverManager.getConnection(connectionURL, user, pass);
	}

	/**
	 * Execute a query select, ... an return a resultset
	 * @param query Select query
	 * @return      Query result
	 * @throws SQLException
	 */
	public ResultSet query(String query) throws SQLException {
		return statement.executeQuery(query);
	}

	/**
	 * Execute a update query: after table, update table, delete table
	 * @param query Select query
	 * @return      an integer number of rows in table after
	 * @throws SQLException
	 */
	public int update(String query) throws SQLException {
		return statement.executeUpdate(query);
	}

}
