package acqua.util.odbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

public class FetchPickingOrder extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		HashMap<String, Object> response = new HashMap<String, Object>();
		
		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		String pickingId = message.getInvocationProperty("pickCode");
		String sociedad = message.getInvocationProperty("sociedad");
		
		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Object connect = manager.connect();
		// Create a response layout
		
		if (sociedad == null)
		{
			response.put("mensaje", "Sociedad no mapeada");
			return response;

		}
		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			response.put("mensaje", "Fallo la conexion a DB. "+connect.toString());
			return response;
		}
		System.out.println("Connection to HANA successful!");
		try {
			// Create a statement to call
			manager.createStatement();

			// Query
			String Query = "SELECT \"AbsEntry\", \"Status\" FROM "+sociedad+".OPKL Where \"AbsEntry\" = '"+ pickingId + "'";
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

	@SuppressWarnings("unchecked")
	public HashMap<String, Object> parseQuery(ResultSet set) throws SQLException {
		int rows = 0;

		HashMap<String, Object> answer = new HashMap<String, Object>();

		while (set.next() != false) {
			if (rows >=1 ) {
				return answer;
			}
			answer.put("pickingId", set.getInt(1));
			answer.put("status", set.getString(2));
			answer.put("existe", true);
			// System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			answer.put("existe", false);
		}
		return answer;
	}
}
