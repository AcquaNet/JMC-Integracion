package acqua.util.odbc.interno;

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

public class FetchUoMCode extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		//String DBInfo = message.getInvocationProperty("DBInfo");
		
		ArrayList<HashMap<String,Object>> articulosList = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("articulos");;
		ArrayList<HashMap<String,Object>> replacementArticulos = new ArrayList<>();
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
			for (HashMap<String,Object> map : articulosList) {
				String UoMEntryQuery = "SELECT \"IUoMEntry\" FROM "+sociedad+".OITM WHERE \"ItemCode\" = '"+map.get("codigo")+"'";
				System.out.println("Query: " + UoMEntryQuery);
				ResultSet querySet = manager.executeQuery(UoMEntryQuery);
				int UoMEntry = parseQuery(querySet);
				String UoMCodeQuery = "SELECT \"UomCode\" FROM "+sociedad+".OUOM WHERE \"UomEntry\" = '"+UoMEntry+"'";
				System.out.println("Query: " + UoMCodeQuery);
				ResultSet UoMCodeSet = manager.executeQuery(UoMCodeQuery);
				String UoMCode = parseUoMCode(UoMCodeSet);
				//LOG.info("Parsing done!");
				System.out.println("UoM Code is "+UoMCode);
				map.put("UoMCode", UoMCode);
				replacementArticulos.add(map);
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

	@SuppressWarnings("unchecked")
	public int parseQuery(ResultSet set) throws SQLException {
		int rows = 0;
		int UoMEntry = -1;

		while (set.next() != false) {
			UoMEntry = set.getInt(1);
			//System.out.println(rowResult);
			rows++;
		}
			if (rows == 0) {
				return -1;
		}
		return UoMEntry;
	}
	
	@SuppressWarnings("unchecked")
	public String parseUoMCode(ResultSet set) throws SQLException {
		int rows = 0;
		String UoMCode = "Manual";

		while (set.next() != false) {
			UoMCode = set.getString("UomCode");
			System.out.println(UoMCode);
			rows++;
		}
			if (rows == 0) {
				return "Manual";
		}
		return UoMCode;
	}
}
