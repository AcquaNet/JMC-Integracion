package sapb1masterpoll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ODBCManager {
	private String user;
	private String pass;
	private String dbstring;
	private Connection connection;
	private Statement stmt;
	
	// Define all the database information
	public ODBCManager(String user, String pass, String dbstring) {
		this.user = user;
		this.pass = pass;
		this.dbstring = dbstring;
	}

	public Connection connect() {
		connection = null;
		try {
		
			//System.out.println("Loading in: "+dbstring+" "+user+" "+pass);
			connection = DriverManager.getConnection(dbstring, user, pass);
		} catch (SQLException e) {
			System.err.println("Connection Failed. User/Passwd Error? Message: " + e.getMessage());
			return null;
		} 
		return connection;
		
	}
	
	public void createStatement() throws SQLException {
		stmt = connection.createStatement();
	}
	
	public ResultSet executeQuery(String Query) throws SQLException {
		return stmt.executeQuery(Query);
	}
}
