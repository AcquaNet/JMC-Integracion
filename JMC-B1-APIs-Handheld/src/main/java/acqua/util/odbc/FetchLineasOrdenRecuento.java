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

public class FetchLineasOrdenRecuento extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		String sociedad = message.getInvocationProperty("sociedad");
		String codigoInterno = ""+message.getInvocationProperty("codigoInterno");

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
			String Query = "SELECT \"LineNum\", \"ItemCode\", \"Counted\", \"CountQty\", \"WhsCode\", \"BinEntry\", \"UomCode\"  FROM "+sociedad+".INC1 where \"DocEntry\" = '"+codigoInterno+"'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			ArrayList<HashMap<String, Object>> queryResult = parseQuery(querySet);
			LOG.info("Parsing done!");

			return queryResult;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<HashMap<String, Object>> parseQuery(ResultSet set) throws SQLException {
		int rows = 0;

		ArrayList<HashMap<String, Object>> answer = new ArrayList<HashMap<String, Object>>();

		Boolean found = false;
		while (set.next() != false) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (set.getString(2).equals("A")) {
				found = true;
			}
			else {
				if (found) {
					map.put("LineNumber", set.getInt(1) - 1);
				}
				else
				{
					map.put("LineNumber", set.getInt(1));
				}
				map.put("ItemCode", set.getString(2));
				map.put("Counted",	set.getString(3));
				map.put("CountedQuantity", Double.parseDouble(set.getString(4)));
				map.put("WarehouseCode", set.getString(5));
				map.put("BinEntry", set.getString(6));
				map.put("UoMCode",	set.getString(7));
				answer.add(map);
			}


			// System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			
		}
		return answer;
	}
}
