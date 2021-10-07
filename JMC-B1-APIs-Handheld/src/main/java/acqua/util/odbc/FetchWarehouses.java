package acqua.util.odbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

public class FetchWarehouses extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	//Remove for debug
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String userRole = message.getInvocationProperty("userRole");
		HashMap<String, Object> input = message.getInvocationProperty("input");

		String sociedad = (String) input.get("sociedad");
		// Create a connection manager with all the info
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
			
			// Query
			String QueryRol = "SELECT \"Almacen\" FROM "+sociedad+".\"@ZHHALMACEN\" WHERE \"Rol\" = '"+ userRole + "'";
			System.out.println("Query: " + QueryRol);
			ResultSet querySetRole = manager.executeQuery(QueryRol);
			
			HashMap<String, Object> roleWResult = parseRoleWarehouses(querySetRole);
			
			ArrayList<String> whsList =  (ArrayList<String>) roleWResult.get("Warehouses");
			
			
			
			// Query
			String Query = "SELECT \"WhsCode\", \"WhsName\" FROM "+sociedad+".OWHS WHERE \"WhsCode\" IN (" + String.join(",", whsList) + ")";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			HashMap<String, Object> queryResult = parseQuery(querySet);
			LOG.info("Parsing done!");

			response.put("result", queryResult.get("result"));
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}


	public HashMap<String, Object> parseQuery(ResultSet set) throws SQLException {
		int rows = 0;
		HashMap<String, Object> map = new HashMap<String, Object>();
		ArrayList<HashMap<String,String>> warehouses = new ArrayList<HashMap<String,String>>();
		
		while (set.next() != false) {
			ResultSetMetaData rsmd = set.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			String rowResult = "";
			for (int i = 1; i <= columnsNumber; i++) {
				String result = set.getString(i);
				String column = rsmd.getColumnName(i);
				map.put(column, result);
				rowResult = rowResult + "|" + column + ":" + result;
			}
			HashMap<String, String> whs = new HashMap<String,String>();
			whs.put("WhsCode", set.getString(1));
			whs.put("WhsName", set.getString(2));
			warehouses.add(whs);
			System.out.println(rowResult);
			rows++;
		}
			if (rows == 0) {
			map = new HashMap<String, Object>();
			map.put("Error", true);
			map.put("ErrorMessage", "No existen ubicaciones para este almacen");
		}
		map.put("result", warehouses);
		return map;
	}



	public HashMap<String, Object> parseRoleWarehouses(ResultSet set) throws SQLException {
		int rows = 0;
		HashMap<String, Object> queryResult = new HashMap<String, Object>();
		HashMap<String, Object> response = new HashMap<String, Object>();
		ArrayList<String> whsList = new ArrayList<String>();
		
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
			whsList.add(set.getString(1));
			System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			response.put("Error", true);
			response.put("ErrorMessage", "No existen ubicaciones para este almacen");
		}
		response.put("Warehouses", whsList);
		return response;
	}
		

}
