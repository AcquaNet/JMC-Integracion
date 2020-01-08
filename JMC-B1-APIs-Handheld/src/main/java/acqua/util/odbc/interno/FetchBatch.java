package acqua.util.odbc.interno;

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

public class FetchBatch extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		
		ArrayList<HashMap<String,Object>> articulosList = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("articulos");;
		HashMap<String,Object> replacementArticulos = new HashMap<>();
		String sociedad = (String)  message.getInvocationProperty("sociedad");
		String origen = message.getInvocationProperty("origen");
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
			for (HashMap<String,Object> map : articulosList) {
				String UoMEntryQuery = "SELECT  \"DistNumber\", \"OnHandQty\" FROM "+sociedad+".OBBQ T0 INNER JOIN "+sociedad+".OBIN T1 ON T0.\"BinAbs\" = T1.\"AbsEntry\""
						+ " INNER JOIN "+sociedad+".OBTN T2 ON T0.\"SnBMDAbs\" = T2.\"AbsEntry\""
						+ " WHERE T2.\"ItemCode\" = '"+map.get("codigo")+"' AND  T1.\"AbsEntry\" = '"+origen+"' "
						+ "AND T0.\"OnHandQty\" > '0'"
						+ "ORDER BY T2.\"InDate\"";
				
				System.out.println("Query: " + UoMEntryQuery);
				ResultSet querySet = manager.executeQuery(UoMEntryQuery);
				HashMap<String,Object> results = parseQuery(querySet);
				//LOG.info("Parsing done!");
				
				replacementArticulos.put((String) map.get("codigo"), results);
			}
			return replacementArticulos;
			
			} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return response;
	}

	public HashMap<String, Object> parseQuery(ResultSet set) throws SQLException {
		HashMap<String, Object> results = new HashMap<>();

		int rows = 0;
		while (set.next() != false) {
			results.put(set.getString("DistNumber"), set.getString("OnHandQty"));
			rows++;
		}
		if (rows == 0) {
				return results;
		}
		return results;
	}
	
}
