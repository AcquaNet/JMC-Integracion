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

public class FetchPickingOrderLines extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		Integer pickingId = Integer.valueOf(message.getInvocationProperty("pickCode"));
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

			// Order Query
			String OrderQuery = "SELECT \"AbsEntry\", \"PickEntry\", \"ItemCode\", \"RelQtty\", \"PickQtty\" FROM "
					+ sociedad + ".PKL2 WHERE \"AbsEntry\" = '"+pickingId+"'";
			System.out.println("Query: " + OrderQuery);
			ResultSet OrderQuerySet = manager.executeQuery(OrderQuery);
			ArrayList<HashMap<String, Object>> OrderQueryResult = parseQuery(OrderQuerySet);
		
			// Transfer Query
//			String TransferQuery = "SELECT \"LineNum\", \"ReleasQtty\", \"PickStatus\", \"PickOty\", \"PickIdNo\", \"ItemCode\" FROM "
//					+ sociedad + ".WTQ1 WHERE \"PickIdNo\" = '"+pickingId+"'";
//			System.out.println("Query: " + OrderQuery);
//			ResultSet TransferQuerySet = manager.executeQuery(TransferQuery);
//			ArrayList<HashMap<String, Object>> TransferQueryResult = parseQuery(TransferQuerySet);
			
			
			//OrderQueryResult
			LOG.info("Parsing done!");

			return OrderQueryResult;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}

	public ArrayList<HashMap<String, Object>> parseQuery(ResultSet set) throws SQLException {
		ArrayList<HashMap<String, Object>> orderLines = new ArrayList<>();

		while (set.next() != false) {
			HashMap<String, Object> answer = new HashMap<String, Object>();

			answer.put("linenum", set.getInt("PickEntry"));
			answer.put("cantidad", Double.valueOf(set.getString("RelQtty")));
			answer.put("pickeado", Double.valueOf(set.getString("PickQtty")));
			answer.put("picklist", Double.valueOf(set.getString("AbsEntry")));
			answer.put("codigo", (set.getString("ItemCode")));
			orderLines.add(answer);
			
		}

		return orderLines;
	}
}
