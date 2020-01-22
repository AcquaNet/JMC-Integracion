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

public class FetchAbsEntry extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		String binCode = (String)  message.getInvocationProperty("BinCode");
		String sociedad = (String)  message.getInvocationProperty("sociedad");
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
			String Query = "SELECT \"AbsEntry\" FROM "+sociedad+".OBIN WHERE \"BinCode\" = '"+binCode+"'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			int queryResult = parseQuery(querySet);
			//LOG.info("Parsing done!");
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
	public int parseQuery(ResultSet set) throws SQLException {
		int rows = 0;
		int AbsEntry = -1;

		while (set.next() != false) {
			AbsEntry = set.getInt("AbsEntry");
			//System.out.println(rowResult);
			rows++;
		}
			if (rows == 0) {
				return -1;
		}
		return AbsEntry;
	}
}