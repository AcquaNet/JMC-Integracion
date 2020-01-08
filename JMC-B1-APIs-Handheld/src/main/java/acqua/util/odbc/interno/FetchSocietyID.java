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

public class FetchSocietyID extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String entorno = message.getInvocationProperty("entorno");
		String DBInfo = message.getInvocationProperty("DBInfo");
		String sociedad = (String)  message.getInvocationProperty("socID");
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
			String Query = "SELECT \"Name\" FROM "+DBInfo+".\"@ZHHEMPRESA\" WHERE \"U_HHSociedad\" = '"+sociedad+"' AND \"U_HHEntorno\" = '"+entorno+"'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			String queryResult = parseQuery(querySet);
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
	public String parseQuery(ResultSet set) throws SQLException {
		int rows = 0;
		String code = "";

		while (set.next() != false) {
			code = set.getString("Name");
			//System.out.println(rowResult);
			rows++;
		}
			if (rows == 0) {
				return null;
		}
		return code;
	}
}
