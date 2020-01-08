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

public class FetchCurrentStock extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		String origen = message.getInvocationProperty("origen");
		String destino = message.getInvocationProperty("destino");

		ArrayList<HashMap<String,Object>> articulosList = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("articulos");
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
				
				// First fetch stock for origin
				String stockOrigenQuery = "SELECT "+sociedad+".OIBQ.\"ItemCode\", "+sociedad+".OIBQ.\"BinAbs\", "+sociedad+".OIBQ.\"OnHandQty\", "+sociedad+".OIBQ.\"WhsCode\""
						+ ", "+sociedad+".OIBQ.\"Freezed\", "+sociedad+".OBIN.\"BinCode\""
						+ ", "+sociedad+".OITM.\"SWW\" "
						+ " FROM " + sociedad +".OIBQ, "+sociedad+".OBIN,  "+sociedad+".OITM"
						+ " WHERE "+ sociedad +".OIBQ.\"BinAbs\" = "+sociedad+".OBIN.\"AbsEntry\""
						+ " AND "  + sociedad +".OIBQ.\"ItemCode\" = "+sociedad+".OITM.\"ItemCode\""
						+ " AND "  + sociedad +".OIBQ.\"Freezed\" = 'N' "
						+ " AND "  + sociedad +".OIBQ.\"BinAbs\" = '"+origen+"'"
						+ " AND "  + sociedad +".OIBQ.\"ItemCode\" = '"+map.get("codigo")+"'";
				System.out.println("Query: " + stockOrigenQuery);
				ResultSet querySet = manager.executeQuery(stockOrigenQuery);
				Double stockOrigen = parseQuery(querySet);
				System.out.println("Stock en Origen: "+stockOrigen);
				// Set the new difference and check if the results are OK
				Double stockMove = (Double) map.get("cantidad");
				System.out.println("Stock moviendose: "+stockMove);
				if ((stockOrigen - stockMove) >= 0.0) {
					message.setInvocationProperty("result", true);
					stockOrigen = stockOrigen - stockMove;
				}
				else
				{
					message.setInvocationProperty("result", false);
					message.setInvocationProperty("cause",  "No hay esa cantidad de stock disponible.");
					return null;
				}
				
				String stockDestinoQuery = "SELECT "+sociedad+".OIBQ.\"ItemCode\", "+sociedad+".OIBQ.\"BinAbs\", "+sociedad+".OIBQ.\"OnHandQty\", "+sociedad+".OIBQ.\"WhsCode\""
						+ ", "+sociedad+".OIBQ.\"Freezed\", "+sociedad+".OBIN.\"BinCode\""
						+ ", "+sociedad+".OITM.\"SWW\" "
						+ " FROM " + sociedad +".OIBQ, "+sociedad+".OBIN,  "+sociedad+".OITM"
						+ " WHERE "+ sociedad +".OIBQ.\"BinAbs\" = "+sociedad+".OBIN.\"AbsEntry\""
						+ " AND "  + sociedad +".OIBQ.\"ItemCode\" = "+sociedad+".OITM.\"ItemCode\""
						+ " AND "  + sociedad +".OIBQ.\"Freezed\" = 'N' "
						+ " AND "  + sociedad +".OIBQ.\"BinAbs\" = '"+destino+"'"
						+ " AND "  + sociedad +".OIBQ.\"ItemCode\" = '"+map.get("codigo")+"'";
				System.out.println("Query: " + stockDestinoQuery);
				ResultSet UoMCodeSet = manager.executeQuery(stockDestinoQuery);
				Double stockDestino = parseQuery(UoMCodeSet);
				System.out.println("Stock Destino "+stockDestino);
				stockDestino = stockDestino + stockMove;
				
				//LOG.info("Parsing done!");
				
				// Seteo valores para origen y agrego al body
				map.put("ubicacionOrigen", origen);
				map.put("ubicacionDestino", destino);
				map.put("cantidad", stockMove);

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

	public Double parseQuery(ResultSet set) throws SQLException {
		int rows = 0;
		Double UoMEntry = 0.0;

		while (set.next() != false) {
			UoMEntry = set.getDouble("OnHandQty");
			//System.out.println(rowResult);
			rows++;
		}
			if (rows == 0) {
				return 0.0;
		}
		return UoMEntry;
	}
}
