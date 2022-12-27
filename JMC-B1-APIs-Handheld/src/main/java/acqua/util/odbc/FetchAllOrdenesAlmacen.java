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

public class FetchAllOrdenesAlmacen extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		String codigo = message.getInvocationProperty("codigo");
		String sociedad = message.getInvocationProperty("sociedad");

		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Object connect = manager.connect();
		// Create a response layout
		HashMap<String, Object> response = new HashMap<String, Object>();

		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			return "Error Connecting to DB. "+connect.toString();
		}
		System.out.println("Connection to HANA successful!");
		try {
			// Create a statement to call
			manager.createStatement();

			// Fetch OIBQ data
			String OrderQuery = "SELECT " + sociedad + ".OINC.\"DocNum\", "+sociedad+".INC1.\"WhsCode\" FROM " + sociedad + ".OINC, " + sociedad
					+ ".INC1 WHERE " + sociedad + ".OINC.\"DocEntry\" = " + sociedad + ".INC1.\"DocEntry\"" + " AND "
					+ sociedad + ".OINC.\"Status\" = 'O' AND " + sociedad + ".INC1.\"ItemCode\" = 'A'";
			System.out.println("Query: " + OrderQuery);
			ResultSet OrderQuerySet = manager.executeQuery(OrderQuery);
			ArrayList<HashMap<String,Object>> DocNum = parseQuery(OrderQuerySet);

			// OrderQueryResult.addAll(TransferQueryResult);
			LOG.info("Parsing done!");

			return DocNum;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				return "Error de SQL. Revisar";
			}
		}

		return null;
	}

	public ArrayList<HashMap<String, Object>> parseQuery(ResultSet set) throws SQLException {
		ArrayList<HashMap<String, Object>> results = new ArrayList<>();
		while (set.next() != false) {
			HashMap<String,Object> map = new HashMap<>();
			map.put("DocNum", set.getString("WhsCode"));
			map.put("WhsCode", set.getString("WhsCode"));
			results.add(map);
		}
		return results;
	}
}
