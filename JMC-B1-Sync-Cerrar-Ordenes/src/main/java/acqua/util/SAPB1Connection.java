package acqua.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class SAPB1Connection {
	public static String user = "SYSTEM";
	public static String password = "Pwjmcgroup1";
	public static String connectionString = "jdbc:sap://sapb1.jmcgroup.com.ar:30015/";

	public static void main(String[] argv) throws Exception {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(connectionString, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Connection Failed. User/Passwd Error? Message: " + e.getMessage());
			return;
		}
		if (connection != null) {
			try {
				long start = System.currentTimeMillis();
				System.out.println("Connection to HANA successful!");
				Statement stmt = connection.createStatement();
				// ResultSet resultSet = stmt.executeQuery("SELECT * FROM SD_DEV.OITW");
				ResultSet resultSet = stmt.executeQuery("SELECT \"ItemCode\",\"InvntItem\" FROM SD_DEV.OITM");
				HashMap<String, Boolean> isInventory = new HashMap<String,Boolean>();
				while (resultSet.next() != false) {
					String ItemCode = resultSet.getString("ItemCode");
					String Inventory = resultSet.getString("InvntItem");
					if (Inventory.equals("Y")) {
						isInventory.put(ItemCode, true);
					}
					else
					{
						isInventory.put(ItemCode, false);
					}
				}
				
				resultSet = stmt.executeQuery("SELECT \"ItemCode\", \"WhsCode\",\"OnHand\", \"IsCommited\", \"OnOrder\",\"createDate\", \"updateDate\" FROM SD_DEV.OITW");
				int columnsNumber = 0;
				int rows = 0;
				ArrayList<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();
				Date currentPoint = new Date();
				Calendar startPoint = new GregorianCalendar();
				// startPoint.setTime(currentPoint);
				// System.out.println(startPoint.get(Calendar.DAY_OF_MONTH));
				long timestampOrig = DateTimeSaver.getUpdateTime("stock", "" + currentPoint.getTime());
				currentPoint.setTime(timestampOrig);
				startPoint.setTime(currentPoint);
				System.out.println("Date:" + startPoint.getTime());
				while (resultSet.next() != false) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					ResultSetMetaData rsmd = resultSet.getMetaData();
					columnsNumber = rsmd.getColumnCount();
					String rowResult = "";
					for (int i = 1; i <= columnsNumber; i++) {
						String result = resultSet.getString(i);
						String column = rsmd.getColumnName(i);
						map.put(column, result);
						// System.out.println(column+":"+result);
						rowResult = rowResult + "|" + column + ":" + result;

					}
					String date = (String) map.get("updateDate");
					if (date == null) {
						date = (String) map.get("createDate");
					}
					if (date != null) {
						// System.out.println(date);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSSSSSS");
						Date dateObj = sdf.parse(date);
						Calendar calendar = new GregorianCalendar();
						calendar.setTime(dateObj);
						if (calendar.after(startPoint) || calendar.equals(startPoint)) {
							// System.out.println("Yes date is after: ");
							map.put("calendar", calendar);
							results.add(map);

						}
					}
					// System.out.println(dateObj);
					rows++;
					//System.out.println(rowResult);

				}

				System.out.println(columnsNumber + " - " + rows);
				System.out.println(System.currentTimeMillis() - start);

				boolean val = false;
				if (val)
					return;
				JSONObject obj = new JSONObject();
				JSONArray array = new JSONArray();
				ArrayList<HashMap<String, Object>> rlist = DateTimeSaver.orderByDate(results);
				int i = 1;
				for (HashMap<String, Object> map : rlist) {
					// System.out.println(((Calendar) map.get("calendar")).getTime());
					JSONObject jsonMap = new JSONObject();
					jsonMap.put("LineNumber", i);
					jsonMap.put("ItemCode", (String) map.get("ItemCode"));
					jsonMap.put("WarehouseCode", (String) map.get("WhsCode"));
					double count = 0;
					count = (Double.valueOf((String) map.get("OnHand")))
							- (Double.valueOf((String) map.get("IsCommited")))
							+ (Double.valueOf((String) map.get("OnOrder")));
					jsonMap.put("CountedQuantity", count);
					if (isInventory.get((String) map.get("ItemCode"))) {
						array.put(jsonMap);
					}
					i++;
				}
				obj.put("InventoryPostingLines", array);
				System.out.println(obj.toString());

			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("Query failed!");
			}
		}

	}
}
