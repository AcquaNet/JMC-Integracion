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

public class FetchArticulosValidosBySociedadDeposito extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String sociedad = message.getInvocationProperty("sociedad");
		String deposito = message.getInvocationProperty("deposito");
		
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

			// Fetch OIBQ data
			//String OrderQuery = "SELECT T0.\"ItemCode\", T0.\"CodeBars\", T0.\"ItemName\", T0.\"SWW\" FROM "+sociedad+".OITM T0 WHERE T0.\"CodeBars\" IS NOT NULL AND T0.\"validFor\" = 'Y'";
			String OrderQuery = "SELECT T0.\"ItemCode\", T0.\"CodeBars\", T0.\"ItemName\", T0.\"SWW\" FROM "+sociedad+".OITM T0 INNER JOIN " +sociedad+ ".OITW T1 ON T0.\"ItemCode\" = T1.\"ItemCode\" WHERE T0.\"CodeBars\" IS NOT NULL AND T0.\"validFor\" = 'Y' AND T1.\"WhsCode\" = '" + deposito + "'";
			System.out.println("Query: " + OrderQuery);
			ResultSet OrderQuerySet = manager.executeQuery(OrderQuery);
			ArrayList<HashMap<String, Object>> OrderQueryResult = parseQuery(OrderQuerySet);
			

			//OrderQueryResult.addAll(TransferQueryResult);
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

			answer.put("codigo", set.getString("ItemCode"));
			answer.put("codigobarra", set.getString("CodeBars"));
			answer.put("descripcion", set.getString("ItemName"));
			answer.put("codigotango", set.getString("SWW"));
			orderLines.add(answer);

		}
		
		System.out.println("Articulos seleccionados: " + orderLines.size());

		return orderLines;
	}
}
