package acqua.util.odbc;

import org.mule.transformer.AbstractMessageTransformer;
import org.mule.api.transformer.TransformerException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;


import acqua.util.ODBCManager;

public class FetchUbicacionesLocal extends AbstractMessageTransformer {
	
	private static final Logger LOG = Logger.getLogger("jms_java.log");
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		// Set DB Login Information from Flowvars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String sociedad = message.getInvocationProperty("sociedad");
		
		// Set request input
		HashMap<String, Object> input = message.getInvocationProperty("input");
		
		
		// Create connection manager with all the db info (required)
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		
		// Connect to DB
		Object connect = manager.connect();
		
		// Create a response layout
		HashMap<String, Object> response = new HashMap<String, Object>();

		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			response.put("mensaje", "Fallo la conexion a DB. "+connect.toString());
			return response;
		}
		System.out.println("Connection to HANA successful!");
		
		try {
			// Create a statement to call
			manager.createStatement();
			
			// Build query string
			String queryString = "SELECT * FROM " + sociedad + ".\"@ZHHLOCAL\"";
			
			System.out.println("Query: " + queryString);
			
			// Execute Query and parse response
			ResultSet querySet = manager.executeQuery(queryString);
			HashMap<String, Object> queryResponse = parseResponse(querySet);
			LOG.info("Parsing done!");
			
			return queryResponse;
			
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}
		
		
		return null;
		
	}
	
	private HashMap<String, Object> parseResponse(ResultSet set) throws SQLException {
		HashMap<String, Object> response = new HashMap<String, Object>();
		
		int rows = 0;
		HashMap<String, Object> queryResult = new HashMap<String, Object>();
		ArrayList<String> locList = new ArrayList<String>();
		
		while (set.next() != false) {
			ResultSetMetaData rsmd = set.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			String rowResult = "";
			for (int i = 1; i <= columnsNumber; i++) {
				String result = set.getString(i);
				String column = rsmd.getColumnName(i);
				queryResult.put(column, result);
				rowResult = rowResult + "|" + column + ":" + result;
				
			}
			locList.add(set.getString(1));
			System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			response.put("Error", true);
			response.put("ErrorMessage", "No existen ubicaciones para este local");
		}
		response.put("Ubicaciones", locList);
		return response;
	}
	
}
