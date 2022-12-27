package acqua.util.odbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

public class FetchUserRoles extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		String DBInfo = message.getInvocationProperty("DBInfo");
		HashMap<String, Object> input = message.getInvocationProperty("input");
		String code = message.getInvocationProperty("code");
		//HHPass = DigestUtils.md5Hex(HHPass);
		
		
		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Object connect = manager.connect();

		
		HashMap<String, Object> response = new HashMap<String, Object>();
		
		
		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			response.put("mensaje", "Fallo la conexion a DB. "+connect.toString());
			return response;
		}
		
		System.out.println("Connection to HANA successful!");
		try {
			// Create a statement to call
			manager.createStatement();

			// Roles Query
			String rolesQuery = "SELECT * FROM "+DBInfo+".\"@ZHHROL\" WHERE \"Code\" = '" + code + "'";
			System.out.println("Query: " + rolesQuery);
			ResultSet users = manager.executeQuery(rolesQuery);
			System.out.println("Parsing users");
			
			HashMap<String, Object> userRoles = parseRoles(users);
			
			return userRoles;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}

	public HashMap<String, Object> parseRoles(ResultSet set) throws SQLException {
		int rows = 0;
		HashMap<String, Object> map = new HashMap<String, Object>();
		while (set.next() != false) {
			ResultSetMetaData rsmd = set.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			String rowResult = "";
			for (int i = 1; i <= columnsNumber; i++) {
				String result = set.getString(i);
				String column = rsmd.getColumnName(i);
				map.put(column, result);
				rowResult = rowResult + "|" + column + ":" + result;
			}

			rows++;
		}
		if (rows == 0) {
			map.put("Error", true);
			map.put("ErrorMessage", "Rol no encontrado en el entorno");
		}
		return map;
	}
}
