package acqua.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class SAPB1Connection {
	public static String user = "SYSTEM";
	public static String password = "Pwjmcgroup1";
	public static String connectionString = "jdbc:sap://sapb1.jmcgroup.com.ar:30015/";

	public static void main(String[] argv) throws Exception {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(connectionString, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Connection Failed. User/Passwd Error? Message: " + e.getMessage());
			return;
		}
		if (connection != null) {
			try {
				long start = System.currentTimeMillis();
				System.out.println("Connection to HANA successful!");
				Statement stmt = connection.createStatement();
				// ResultSet resultSet = stmt.executeQuery("SELECT * FROM SD_DEV.OITW");
				ResultSet resultSet = stmt.executeQuery("SELECT * FROM JMCG_DEV8.\"@ZHHROL\"");
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				while (resultSet.next()) {
				    for (int i = 1; i <= columnsNumber; i++) {
				        if (i > 1) System.out.print(",  ");
				        String columnValue = resultSet.getString(i);
				        String columnType = rsmd.getColumnTypeName(i);
				        System.out.print(rsmd.getColumnName(i) +":"+ columnValue + ":" + columnType);
				    }
				    System.out.println("");
				}

			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Query failed!");
			}
		}

	}
}
