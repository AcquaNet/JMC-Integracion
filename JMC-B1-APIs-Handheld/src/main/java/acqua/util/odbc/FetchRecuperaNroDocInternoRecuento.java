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

public class FetchRecuperaNroDocInternoRecuento extends AbstractMessageTransformer {
	
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		//String DBInfo = message.getInvocationProperty("DBInfo");
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

			// si la consulta es offline - no recupera doc interno - debo buscarlo
			if(codigoInterno.equals("0")) {
				String Query = "SELECT  T0.\"DocEntry\" FROM "+sociedad+".OINC T0 INNER JOIN " +sociedad+ ".INC1 T1 ON T0.\"DocEntry\" = T1.\"DocEntry\" WHERE T0.\"DocNum\" = '"+codigo + "'";
				System.out.println("Query: " + Query);
				ResultSet querySet = manager.executeQuery(Query);

				LOG.info("Parsing done!");

				return  parseQuery(querySet);
			}
			else {
				return codigoInterno;
			}
			
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

		if (set.next() != false) {
			return set.getString(1);
		}
		else {
			return "";
		}
	}		

}
