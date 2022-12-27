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

public class FetchLineasOrdenRecuento extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		String sociedad = message.getInvocationProperty("sociedad");
		String codigoInterno = ""+message.getInvocationProperty("codigoInterno");
		String codigo = ""+message.getInvocationProperty("codigo");

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

			String Query = "";
			// si la consulta es offline - no recupera doc interno - debo buscarlo
			if(codigoInterno.equals("0")) {
				codigoInterno = getNumeroInterno(codigo, sociedad, manager);
			}

			// Query
			Query = "SELECT  T0.\"LineNum\",  T0.\"ItemCode\",  T0.\"Counted\",  T0.\"CountQty\",  T0.\"WhsCode\",  T0.\"BinEntry\",  T0.\"UomCode\"  FROM "+sociedad+".INC1 T0 where  T0.\"DocEntry\" = '"+codigoInterno+"'";
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

		Boolean found = false;
		while (set.next() != false) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (set.getString(2).equals("A")) {
				found = true;
			}
			else {
				if (found) {
					map.put("LineNumber", set.getInt(1) - 1);
				}
				else
				{
					map.put("LineNumber", set.getInt(1));
				}
				map.put("ItemCode", set.getString(2));
				map.put("Counted",	set.getString(3));
				map.put("CountedQuantity", Double.parseDouble(set.getString(4)));
				map.put("WarehouseCode", set.getString(5));
				map.put("BinEntry", set.getString(6));
				map.put("UoMCode",	set.getString(7));
				answer.add(map);
			}


			// System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			
		}
		return answer;
	}
	
	private String getNumeroInterno(String nroOrden, String sociedad, ODBCManager manager) throws SQLException {
		
		String Query = "SELECT  T0.\"DocEntry\" FROM "+sociedad+".OINC T0 INNER JOIN " +sociedad+ ".INC1 T1 ON T0.\"DocEntry\" = T1.\"DocEntry\" WHERE T0.\"DocNum\" = '"+nroOrden + "'";
		System.out.println("Query: " + Query);
		ResultSet querySet = manager.executeQuery(Query);

		LOG.info("Parsing done!");

		return  parseQuery2(querySet);
		
	}
	
	
	@SuppressWarnings("unchecked")
	public String parseQuery2(ResultSet set) throws SQLException {

		if (set.next() != false) {
			return set.getString(1);
		}
		else {
			return "";
		}
	}	
}
