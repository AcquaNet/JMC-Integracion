package acqua.util.odbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

public class FetchUserTable extends AbstractMessageTransformer {
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

		String HHUser = (String) input.get("user");
		String HHPass = (String) input.get("pass");
		//HHPass = DigestUtils.md5Hex(HHPass);

		String HHEntorno = (String) input.get("entorno");
		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Object connect = manager.connect();

		// Create a response layout
		HashMap<String, Object> response = new HashMap<String, Object>();
		response.put("user", HHUser);
		response.put("pass", HHPass);
		response.put("entorno", HHEntorno);
		response.put("login", false);
		response.put("recepcion", false);
		response.put("picking", false);
		response.put("transferencia", false);
		response.put("recuento", false);
		response.put("mensaje", false);
		response.put("sociedades", "");
		
		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			response.put("mensaje", "Fallo la conexion a DB. "+connect.toString());
			return response;
		}
		
		System.out.println("Connection to HANA successful!");
		try {
			// Create a statement to call
			manager.createStatement();

			// User Query
			String userQuery = "SELECT * FROM "+DBInfo+".\"@ZHHUSUARIO\" WHERE \"U_HHUser\" = '" + HHUser
					+ "' AND \"U_HHEntorno\" = '" + HHEntorno + "' AND \"U_HHActivo\" = 'Y'";
			System.out.println("Query: " + userQuery);
			ResultSet users = manager.executeQuery(userQuery);
			System.out.println("Parsing users");
			HashMap<String, Object> userResult = parseUsers(users);

			// Column: Code: NVARCHAR:50
			// Column: Name: NVARCHAR:100
			// Column: U_HHNombre: NVARCHAR:20
			// Column: U_HHApellido: NVARCHAR:20
			// Column: U_HHRol: NVARCHAR:50
			// Column: U_HHActivo: NVARCHAR:1

			System.out.println("checking user result");
			if (userResult.containsKey("Error")) {
				System.out.println("Error user found");
				response.put("mensaje", userResult.get("ErrorMessage"));
				return response;
			}
			if (userResult.get("U_HHActivo").equals("N")) {
				response.put("mensaje", "Usuario no activo");
				return response;
			}

			if (!userResult.get("U_HHPass").equals(HHPass)) {
				response.put("mensaje", "password incorrecta");
				return response;
			}
			if ((userResult.containsKey("U_HHRol")) ? (userResult.get("U_HHRol") == null) : true ) {
				response.put("mensaje", "Rol no asignado");
				return response;
			}
//			if (!(userResult.get("U_HHSociedad") == null)) {
//				response.put("sociedades", userResult.get("U_HHSociedad"));
//			}
			//response.put("sociedades", userResult.get("U_HHSociedad"));
			// Role Query
			String roleQuery = "SELECT * FROM "+DBInfo+".\"@ZHHROL\" WHERE \"Code\" = '" + userResult.get("U_HHRol") + "'";
			System.out.println("Query: " + roleQuery);
			ResultSet roles = manager.executeQuery(roleQuery);

			System.out.println("Parsing roles");
			HashMap<String, Object> roleResult = parseRoles(roles);

			// Column: Code: NVARCHAR:50
			// Column: Name: NVARCHAR:100
			// Column: U_HHRecepcion: NVARCHAR:1
			// Column: U_HHPicking: NVARCHAR:1
			// Column: U_HHTransf: NVARCHAR:1
			// Column: U_HHRecuento: NVARCHAR:1
			// Column: U_HHActivo: NVARCHAR:1
			// Column: U_HHEntorno: NVARCHAR:3

			if (roleResult.containsKey("Error")) {
				response.put("mensaje", userResult.get("ErrorMessage"));
				return response;
			}
			if (roleResult.get("U_HHActivo").equals("N")) {
				response.put("mensaje", "Rol no activo");
				return response;
			} else {
				response.put("login", true);
			}
			if (roleResult.get("U_HHPicking").equals("Y")) {
				response.put("picking", true);
			}
			if (roleResult.get("U_HHTransf").equals("Y")) {
				response.put("transferencia", true);
			}
			if (roleResult.get("U_HHRecuento").equals("Y")) {
				response.put("recuento", true);
			}
			if (roleResult.get("U_HHRecepcion").equals("Y")) {
				response.put("recepcion", true);
			}

			if (true)
				return response;
			// Society Query
			//String socQuery = "SELECT  * FROM SD_DEV.\"@ZHHEMPRESA\"";

//			ResultSet society = manager.executeQuery(socQuery);

	//		System.out.println("Parsing society");
//			parseSet(society);

			// Column: Code: NVARCHAR:50
			// Column: Name: NVARCHAR:100
			// Column: U_HHBase: NVARCHAR:128
			// Column: U_HHActivo: NVARCHAR:1
			// Column: U_HHEntorno: NVARCHAR:3

	//		LOG.info("Parsing done!");

		//	return response;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}

	public HashMap<String, Object> parseUsers(ResultSet set) throws SQLException {
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
			System.out.println(rowResult);
			if (rows > 1) {
				map.put("Error", true);
				map.put("ErrorMessage", "Se encontro mas de un usuario");
				return map;
			}

		}
		if (rows == 0) {
			map = new HashMap<String, Object>();
			map.put("Error", true);
			map.put("ErrorMessage", "Usuario no encontrado");
		}
		return map;
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
			System.out.println(rowResult);
			if (rows > 1) {
				map.put("Error", true);
				map.put("ErrorMessage", "Mas de un rol asignado en el entorno");
				return map;
			}
		}
		if (rows == 0) {
			map.put("Error", true);
			map.put("ErrorMessage", "Rol no encontrado en el entorno");
		}
		return map;
	}

	public HashMap<String, Object> parseSoc(ResultSet set) throws SQLException {

		return null;
	}

	public HashMap<String, Boolean> parseSet(ResultSet set) throws SQLException {
		int rows = 0;
		while (set.next() != false) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			ResultSetMetaData rsmd = set.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			String rowResult = "";
			for (int i = 1; i <= columnsNumber; i++) {
				String result = set.getString(i);
				String column = rsmd.getColumnName(i);
				map.put(column, result);
				// System.out.println(column+":"+result);
				rowResult = rows + ":" + rowResult + "|" + column + ":" + result;
				System.out.println(rowResult);
			}

			// System.out.println(dateObj);
			rows++;
			// System.out.println(rowResult);

		}
		return null;
	}
}
