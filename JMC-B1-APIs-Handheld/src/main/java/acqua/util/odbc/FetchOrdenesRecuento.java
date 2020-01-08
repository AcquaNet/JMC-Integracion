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

public class FetchOrdenesRecuento extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
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
			String Query = "SELECT * FROM ("
					+ "SELECT T0.\"DocEntry\", T0.\"DocNum\", T1.\"WhsCode\","
					+ " ROW_NUMBER() OVER (PARTITION BY T0.\"DocEntry\") AS RN"
					+ " FROM "+sociedad+".OINC T0, "+sociedad+".INC1 T1"
					+ " WHERE T0.\"DocEntry\" = T1.\"DocEntry\" AND T0.\"Status\" = 'O' "
					+ ") X WHERE RN = '1'";
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

		while (set.next() != false) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("codigo", set.getString("DocNum"));
			map.put("codigoInterno", set.getString("DocEntry"));
			map.put("almacen", set.getString("WhsCode"));
			answer.add(map);
			// System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			
		}
		return answer;
	}
}
