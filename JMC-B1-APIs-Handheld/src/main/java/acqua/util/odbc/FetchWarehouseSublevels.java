package acqua.util.odbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

public class FetchWarehouseSublevels extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		//HashMap<String, Object> input = message.getInvocationProperty("input");

		//String warehouse = (String) input.get("almacen");
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
			ArrayList<HashMap<String, Object>> completeOBIN = new ArrayList<>();
			// Get Society List
			String SocietyQuery = "SELECT \"Name\" FROM "+DBInfo+".\"@ZHHEMPRESA\" WHERE \"U_HHActivo\" = 'Y'";
			ResultSet socSet = manager.executeQuery(SocietyQuery);
			ArrayList<String> socList = parseSoc(socSet);
			
			for (String society : socList) {
			// Query
			String Query = "SELECT \"BinCode\", \"WhsCode\", \"SL1Code\", \"SL2Code\", \"SL3Code\", \"SL4Code\", \"AbsEntry\" FROM " + society + ".OBIN WHERE \"Disabled\" <> 'Y'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			ArrayList<HashMap<String, Object>> queryResult = parseQuery(querySet, society);
			completeOBIN.addAll(queryResult);
			
			}
			return completeOBIN;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}

	
	public ArrayList<String> parseSoc(ResultSet set) throws SQLException {
		ArrayList<String> societys = new ArrayList<>();
		while (set.next() != false) {
			societys.add(set.getString(1));
		}
		return societys;
	}
	
	//ArrayList<String> societys = new ArrayList<>();
	public ArrayList<HashMap<String, Object>> parseQuery(ResultSet set, String soc) throws SQLException {
		ArrayList<HashMap<String, Object>> returnList = new ArrayList<>();
		while (set.next() != false) {
			HashMap<String,Object> innerMap = new HashMap<>();
			innerMap.put("id", set.getString(1));
			innerMap.put("sociedad", soc);
			innerMap.put("deposito", set.getString(2));
			innerMap.put("area", set.getString(3));
			innerMap.put("pasillo", set.getString(4));
			innerMap.put("columna", set.getString(5));
			innerMap.put("altura", set.getString(6));
			innerMap.put("codigo", set.getString(7));
			returnList.add(innerMap);
		}
		return returnList;
	}
//	@SuppressWarnings("unchecked")
//	public HashMap<String, Object> parseQuery(ResultSet set) throws SQLException {
//		int rows = 0;
//		HashMap<String, Object> map = new HashMap<String, Object>();
//		HashMap<String, Object> level1Table = new HashMap<>();
//		while (set.next() != false) {
//			ResultSetMetaData rsmd = set.getMetaData();
//			int columnsNumber = rsmd.getColumnCount();
//			String rowResult = "";
//			for (int i = 1; i <= columnsNumber; i++) {
//				String result = set.getString(i);
//				String column = rsmd.getColumnName(i);
//				map.put(column, result);
//				rowResult = rowResult + "|" + column + ":" + result;
//			}
//			//System.out.println(rowResult);
//
//			if (columnsNumber >= 1) {
//				if (!level1Table.containsKey(set.getString(1))) {
//					// System.out.println("Created new level 1 table: "+set.getString(3));
//					HashMap<String, Object> level2Table = new HashMap<String, Object>();
//					level1Table.put(set.getString(1), level2Table);
//				}
//
//				HashMap<String, Object> level2Table = (HashMap<String, Object>) level1Table.get(set.getString(1));
//				if (columnsNumber >= 2) {
//					if (!level2Table.containsKey(set.getString(2))) {
//						// System.out.println("Created new level 2 table: "+set.getString(4));
//						HashMap<String, Object> level3Table = new HashMap<String, Object>();
//						level2Table.put(set.getString(2), level3Table);
//					}
//					HashMap<String, Object> level3Table = (HashMap<String, Object>) level2Table.get(set.getString(2));
//
//					if (columnsNumber >= 3) {
//						if (!level3Table.containsKey(set.getString(3))) {
//							ArrayList<String> level4Table = new ArrayList<String>();
//							level3Table.put(set.getString(3), level4Table);
//						}
//						ArrayList<String> level4Table = (ArrayList<String>) level3Table.get(set.getString(3));
//
//						if (columnsNumber >= 4) {
//							if (!level4Table.contains(set.getString(4))) {
//								if (!(set.getString(4) == null)) {
//									level4Table.add(set.getString(4));
//								}
//							}
//						}
//						level3Table.put(set.getString(3), level4Table);
//					}
//					level2Table.put(set.getString(2), level3Table);
//				}
//				level1Table.put(set.getString(1), level2Table);
//			}
//			rows++;
//		}
//		map.put("result", level1Table);
//		if (rows == 0) {
//			map = new HashMap<String, Object>();
//			map.put("Error", true);
//			map.put("ErrorMessage", "No existen ubicaciones para este almacen");
//		}
//		return map;
//	}
}
