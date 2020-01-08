package sapb1masterpoll;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class FetchStockAlmacen extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");

		String codigo = message.getInvocationProperty("codigo");

		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);

		Connection connect = manager.connect();
		// Create a response layout
		HashMap<String, Object> response = new HashMap<String, Object>();

		if (connect == null) {
			response.put("mensaje", "Fallo la conexion a DB");
			return response;

		}
		System.out.println("Connection to HANA successful!");
		try {

			
			//OrderQueryResult.addAll(TransferQueryResult);
			LOG.info("Parsing done!");
			ArrayList<String> soc = null;
			ArrayList<HashMap<String,Object>> lines = fetchStockForSocieties(soc, manager, codigo);
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}

	
	public ArrayList<HashMap<String,Object>> fetchStockForSocieties(ArrayList<String> soc, ODBCManager manager, String codigo) throws SQLException{
		ArrayList<HashMap<String,Object>> lines = new ArrayList<>();
		for (String str : soc) {
			manager.createStatement();
			ResultSet set = manager.executeQuery("SELECT \"ItemCode\", \"WhsCode\", \"OnHandQty\", \"BinAbs\" FROM "+ str + ".OIBQ WHERE \"OnHandQty\" > 0 AND \"Freezed\" = 'N' AND \"ItemCode\" = '"+codigo+"'");
			while (set.next() != false) {
			HashMap<String,Object> map = new HashMap<>();
			map.put("Sociedad", str);
			map.put("WhsCode", set.getString("WhsCode"));
			map.put("ItemCode", set.getString("ItemCode"));
			map.put("OnHandQty", set.getString("OnHandQty"));
			map.put("BinAbs", set.getString("BinAbs"));
			lines.add(map);
			}
		}
		
		return lines;
	}
}
