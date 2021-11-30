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

public class FetchCurvaArticulos extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String codigo = message.getInvocationProperty("codigo");
		String sociedad = message.getInvocationProperty("sociedad");

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
			String Query = "SELECT T0.\"Code\", T1.\"Code\" as Item, T1.\"Quantity\" FROM " + sociedad + ".\"OITT\" T0 INNER JOIN " + sociedad + ".\"ITT1\" T1 ON T0.\"Code\" = T1.\"Father\" WHERE T0.\"Code\" = '" + codigo + "'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			HashMap<String, Object> queryResult = parseQuery(querySet);
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

	public HashMap<String, Object> parseQuery(ResultSet set) throws SQLException {
		int rows = 0;
		
		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> answer = new HashMap<String, Object>();

		while (set.next() != false) {
			HashMap<String, Object> row = new HashMap<String, Object>();
			row.put("curva", set.getString(1));
			row.put("item", set.getString(2));
			row.put("cantidad", set.getString(3));
			items.add(row);
			// System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			answer.put("Error", true);
			answer.put("ErrorMessage", "No existen articulos para esta curva");
		}
		answer.put("articulos", items);
		return answer;
	}
}
