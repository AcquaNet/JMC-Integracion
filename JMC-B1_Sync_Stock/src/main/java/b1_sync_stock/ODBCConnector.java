package b1_sync_stock;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class ODBCConnector extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Connection connect = manager.connect();
		if (connect == null) {
			return "Error Connecting to DB. Check Logs";
		} else {
			System.out.println("Connection to HANA successful!");
		}

		try {
			// Create a statement to call
			Statement query1St = manager.createStatement();
			// Decide which database we're hitting
			String destination = message.getInvocationProperty("Destination");
			String origin = message.getInvocationProperty("Origin");
			String fullDestination = destination;
			String warehouseMatch = message.getInvocationProperty("warehouseMatch");
			HashMap<String, String> destinationMap = message.getInvocationProperty("TableDestinations");
			// Check which Items are Inventory enabled from Destination and Holding
			String query1 = "SELECT \"ItemCode\",\"InvntItem\" FROM " + destination + ".OITM";
			String query2 = "SELECT \"ItemCode\",\"InvntItem\" FROM " + origin + ".OITM";
			LOG.info("Item Info Q: " + query1);
			LOG.info("Item Info Q2: " + query2);
			ResultSet stockEnabled = query1St.executeQuery(query1);

			// Save a HashMap of all the Items that are inventory enabled
			HashMap<String, Boolean> invItems = parseInventoryResults(stockEnabled);

			Statement query2St = manager.createStatement();
			ResultSet stockEnabledHolding = query2St.executeQuery(query2);

			String queryLote = "SELECT \"ItemCode\", \"WhsCode\", \"OnHand\" FROM "
					+ origin + ".OITW WHERE \"WhsCode\" in " + warehouseMatch + ""
					+ " AND \"OnHand\" > 0";
			LOG.info("Item Count Q: "+queryLote);
			Statement queryLoteSt = manager.createStatement();
			ResultSet parseStockLoteVentas = queryLoteSt.executeQuery(queryLote);

			// Save a HashMap of all the Items that are inventory enabled
			HashMap<String, Boolean> invItemsHolding = parseInventoryResults(stockEnabledHolding);

			HashMap<String, Object> stockLoteVentas = parseStockLoteVentas(parseStockLoteVentas);

			// Dont syncronize if the item is not enabled in holding, will cause an error.
			// Also, if the item doesnt exist
			for (String val : invItems.keySet()) {
				if (invItems.get(val)) { // Dont bother if item is not set to syncronzie anyway
					if (invItemsHolding.containsKey(val)) { // Check if item exists in holding
						if (!invItemsHolding.get(val)) { // Check if item is enabled in holding
							invItems.put(val, false);

						}
					} else {
						invItems.put(val, false);
					}
				}
			}
			// for (String value : invItemsHolding.keySet()) {
			// if (invItems.get(value)) {
			// //LOG.info("Disabled from holding: "+value);
			// }
			// }
			// Get the last updated Date YYYY-MM-DD
			String updateDate = message.getInvocationProperty("updateDate");
			String updateTime = message.getInvocationProperty("updateTime");

			DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("HH-mm-ss");
			DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("HHmm");
			DateTime dateTime = inputFormatter.parseDateTime(updateTime);
			String formattedUpdateTime = outputFormatter.print(dateTime.getMillis());

			// Get all Item Stocks from DB

			// Full STR
			// https://stackoverflow.com/questions/58507324/filtering-out-duplicate-entires-for-older-rows
			// SELECT * FROM (SELECT T0.\"ItemCode\", T0.\"WhsCode\", T0.\"OnHand\",
			// T0.\"IsCommited\", T0.\"OnOrder\", T1.\"DocDate\", T1.\"DocTime\",
			// ROW_NUMBER() OVER (PARTITION BY T0.\"ItemCode\" ORDER BY T1.\"DocTime\" DESC)
			// AS RN FROM KA_DEV6.OITW T0JOIN KA_DEV6.OINM T1 ON T0.\"WhsCode\" = '01' AND
			// T0.\"ItemCode\" = T1.\"ItemCode\" WHERE T1.\"DocDate\" > '2019-10-20' OR
			// (T1.\"DocDate\" = '2019-10-20' AND T1.\"DocTime\" >= '1025')) X WHERE RN = 1
			String str = "SELECT * FROM " + "("
					+ "SELECT T0.\"ItemCode\", T0.\"WhsCode\", T0.\"OnHand\", T0.\"IsCommited\", T0.\"OnOrder\", T1.\"DocDate\", T1.\"DocTime\", T2.\"LastPurPrc\", "
					+ " ROW_NUMBER() OVER (PARTITION BY T0.\"ItemCode\" "
					+ "ORDER BY T1.\"DocDate\",T1.\"DocTime\" DESC) AS RN " + "FROM " + destination + ".OITW T0 JOIN "
					+ destination + ".OINM T1 " + "ON T0.\"WhsCode\" = '01' AND T0.\"ItemCode\" = T1.\"ItemCode\" "
					+ "JOIN " + destination + ".OITM T2 ON T0.\"ItemCode\" = T2.\"ItemCode\" "
					+ "WHERE T1.\"DocDate\" > '" + updateDate + "' OR (T1.\"DocDate\" = '" + updateDate
					+ "' AND T1.\"DocTime\" >= '" + formattedUpdateTime + "')" + ") X WHERE RN = 1 "
					+ "ORDER BY \"DocDate\", \"DocTime\" DESC";
			LOG.info("Query: " + str);
			ResultSet Items = manager.executeQuery(str);

			// Parse results as array list ordered by date from oldest (top) to newest
			// (bottom)
			ArrayList<HashMap<String, Object>> results = parseItemSelect(Items);
			HashMap<String, String> newest = getNewestDate(results);
			if (newest != null) {
				try {
					CurrentTimeSaver.setUpdateTime("STOCK_" + destination, newest.get("Time"), newest.get("Date"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			destination = destinationMap.get(destination);

			String UoMEntryQuery = "SELECT \"IUoMEntry\",\"ItemCode\" FROM " + fullDestination + ".OITM";
			// System.out.println("Query: " + UoMEntryQuery);
			ResultSet querySet = manager.executeQuery(UoMEntryQuery);
			HashMap<String, Integer> UoMEntryCodes = parseUoMList(querySet);
			String UoMCodeQuery = "SELECT \"UomCode\", \"UomEntry\" FROM " + fullDestination + ".OUOM";
			System.out.println("Query: " + UoMCodeQuery);
			ResultSet UoMCodeSet = manager.executeQuery(UoMCodeQuery);
			HashMap<Integer, String> UoMCode = parseUoMCodeList(UoMCodeSet);
			// LOG.info("Parsing done!");

			HashMap<String, String> UoMCodeTable = new HashMap<>();
			for (String invCode : UoMEntryCodes.keySet()) {
				UoMCodeTable.put(invCode, UoMCode.get(UoMEntryCodes.get(invCode)));
			}

			ArrayList<HashMap<String, Object>> resultsWithUoM = parseItemSelect(Items);

			for (HashMap<String, Object> itemMap : results) {
				if (UoMEntryCodes.get((String) itemMap.get("ItemCode")) != -1) {
					itemMap.put("UoMCode", UoMCodeTable.get(itemMap.get("ItemCode")));
					// System.out.println("ItemCode: " + itemMap.get("ItemCode") + " - "+
					// UoMCodeTable.get(itemMap.get("ItemCode")));
					resultsWithUoM.add(itemMap);
				} else {
					resultsWithUoM.add(itemMap);
				}
			}

			// Create a hashmap to hold the arraylist
			LOG.info("Total results: " + resultsWithUoM.size());
			// System.out.println(message);
			LOG.info("Result returned!");
			// LOG.info(""+StringToJSON.javaToJSONToString(result));

			List<List<HashMap<String, Object>>> arraySplit = splitArray(resultsWithUoM, 300);
			ArrayList<String> Documents = new ArrayList<String>();
			for (List<HashMap<String, Object>> array : arraySplit) {
				JSONObject doc = arrayToDocument(array, destination, invItems, stockLoteVentas);
				if (doc != null) {
					Documents.add(doc.toString());
				}
			}

			return Documents;
		} catch (SQLException | NumberFormatException | ParseException e) {
			e.printStackTrace();
			return e;
		}
	}

	private HashMap<String, Object> parseStockLoteVentas(ResultSet set) throws SQLException {
		HashMap<String, Object> cantidadStock = new HashMap<>();
		while (set.next() != false) {
			if (cantidadStock.containsKey(set.getString("ItemCode"))) {
				HashMap<String, Object> stockMap = (HashMap<String, Object>) cantidadStock
						.get(set.getString("ItemCode"));
				stockMap.put(set.getString("WhsCode"), set.getDouble("OnHand"));
				cantidadStock.put(set.getString("ItemCode"), stockMap);
			} else {
				HashMap<String, Object> stockMap = new HashMap<>();
				stockMap.put(set.getString("WhsCode"), set.getDouble("OnHand"));
				cantidadStock.put(set.getString("ItemCode"), stockMap);
			}
		}
		return cantidadStock;
	}

	public HashMap<String, Integer> parseUoMList(ResultSet set) throws SQLException {
		int rows = 0;

		HashMap<String, Integer> results = new HashMap<>();
		while (set.next() != false) {
			results.put(set.getString("ItemCode"), set.getInt("IUoMEntry"));
			// System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			return null;
		}
		return results;
	}

	public HashMap<Integer, String> parseUoMCodeList(ResultSet set) throws SQLException {
		int rows = 0;
		HashMap<Integer, String> results = new HashMap<>();
		while (set.next() != false) {
			results.put(set.getInt("UomEntry"), set.getString("UomCode"));
			// System.out.println(rowResult);
			rows++;
		}
		if (rows == 0) {
			return null;
		}
		return results;
	}

	public static HashMap<String, String> getNewestDate(ArrayList<HashMap<String, Object>> results) {
		Calendar cal = null;
		for (HashMap<String, Object> map : results) {
			Calendar calendar = (Calendar) map.get("calendar");

			if (cal != null) {
				if (calendar.after(cal)) {
					cal = calendar;
					LOG.info("Date " + getDateFromCalendar(calendar) + " is newer than " + getDateFromCalendar(cal));
				}
			} else {
				cal = calendar;
				LOG.info("Date doesnt exist, date set: " + getDateFromCalendar(calendar));
				LOG.info("Time doesnt exist, date set: " + getTimeFromCalendar(calendar));
			}
		}
		if (cal == null) {
			return null;
		}
		HashMap<String, String> returnInfo = new HashMap<>();
		returnInfo.put("Date", getDateFromCalendar(cal));
		returnInfo.put("Time", getTimeFromCalendar(cal));
		LOG.info(returnInfo.toString());
		return returnInfo;
	}

	public static String getTimeFromCalendar(Calendar cal) {
		LOG.info("Hour: " + cal.get(Calendar.HOUR_OF_DAY));
		LOG.info("Minute: " + (cal.get(Calendar.MINUTE)));
		LOG.info("FormatH: " + String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
		return String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + "-"
				+ String.format("%02d", (cal.get(Calendar.MINUTE))) + "-" + "00";
	}

	public static String getDateFromCalendar(Calendar cal) {
		return cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject arrayToDocument(List<HashMap<String, Object>> inputArray, String destination,
			HashMap<String, Boolean> invItems, HashMap<String, Object> stockVentas) {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();

		LOG.info("New Document");
		int i = 1;
		for (HashMap<String, Object> map : inputArray) {
			// System.out.println(((Calendar) map.get("calendar")).getTime());

			JSONObject jsonMap = new JSONObject();
			jsonMap.put("LineNumber", i);
			jsonMap.put("ItemCode", (String) map.get("ItemCode"));
			if (map.get("UoMCode") != null) {
				jsonMap.put("UoMCode", (String) map.get("UoMCode"));
			}
			jsonMap.put("Price", map.get("Price"));
			jsonMap.put("WarehouseCode", destination + "_" + (String) map.get("WharehouseCode"));
			double count = 0;
			count = (Double) map.get("CountedQuantity");
			jsonMap.put("CountedQuantity", count);
			if (invItems.get((String) map.get("ItemCode"))) {
				// LOG.info("Line number: " + i);
				Double cantidadDisponible = 0.0;
				if (stockVentas.containsKey((String) map.get("ItemCode"))) {
					if (((HashMap<String, Object>) stockVentas.get((String) map.get("ItemCode"))).containsKey(destination + "_" + (String) map.get("WharehouseCode"))) {
						cantidadDisponible = (Double) ((HashMap<String, Object>) stockVentas
								.get((String) map.get("ItemCode"))).get(destination + "_" + (String) map.get("WharehouseCode"));
					}
				}
				JSONArray BatchNumbers = new JSONArray();
				JSONObject batchLine = new JSONObject();
				batchLine.put("BatchNumber", "ventas");
				batchLine.put("Quantity", count - cantidadDisponible);
				batchLine.put("BaseLineNumber", i);
				BatchNumbers.put(batchLine);
				jsonMap.put("InventoryPostingBatchNumbers", BatchNumbers);
				
				if (Double.valueOf((String) map.get("Price")) > 0) {
				array.put(jsonMap);
				i++;
				}
			}

		}
		
		obj.put("InventoryPostingLines", array);

		if (i == 1) {
			return null;
		}
		return obj;
	}

	public static List<List<HashMap<String, Object>>> splitArray(ArrayList<HashMap<String, Object>> arrayToSplit,
			int chunkSize) {
		if (chunkSize <= 0) {
			return null; // just in case :)
		}
		// editado de
		// https://stackoverflow.com/questions/27857011/how-to-split-a-string-array-into-small-chunk-arrays-in-java
		// first we have to check if the array can be split in multiple
		// arrays of equal 'chunk' size
		int rest = arrayToSplit.size() % chunkSize; // if rest>0 then our last array will have less elements than the
													// others
		// then we check in how many arrays we can split our input array
		int chunks = arrayToSplit.size() / chunkSize + (rest > 0 ? 1 : 0); // we may have to add an additional array for
																			// the 'rest'
		// now we know how many arrays we need and create our result array
		List<List<HashMap<String, Object>>> arrays = new ArrayList<List<HashMap<String, Object>>>();
		// we create our resulting arrays by copying the corresponding
		// part from the input array. If we have a rest (rest>0), then
		// the last array will have less elements than the others. This
		// needs to be handled separately, so we iterate 1 times less.
		for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
			// this copies 'chunk' times 'chunkSize' elements into a new array
			List<HashMap<String, Object>> array = arrayToSplit.subList(i * chunkSize, i * chunkSize + chunkSize);
			arrays.add(array);
		}
		if (rest > 0) { // only when we have a rest
			// we copy the remaining elements into the last chunk
			// arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) *
			// chunkSize, (chunks - 1) * chunkSize + rest);
			List<HashMap<String, Object>> array = arrayToSplit.subList((chunks - 1) * chunkSize,
					(chunks - 1) * chunkSize + rest);
			arrays.add(array);
		}
		return arrays; // that's it
	}

	public HashMap<String, Boolean> parseInventoryResults(ResultSet set) throws SQLException {
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		while (set.next() != false) {
			String ItemCode = set.getString("ItemCode");
			String Inventory = set.getString("InvntItem");
			if (Inventory.equals("Y")) { // Enabled
				map.put(ItemCode, true);
			} else // Not Enabled
			{
				map.put(ItemCode, false);
			}
		}
		return map;
	}

	public ArrayList<HashMap<String, Object>> parseItemSelect(ResultSet set)
			throws NumberFormatException, SQLException, ParseException {
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
		while (set.next() != false) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("ItemCode", set.getString("ItemCode"));
			map.put("WharehouseCode", set.getString("WhsCode"));
			double count = 0;
			count = (Double.valueOf((String) set.getString("OnHand")))
					- (Double.valueOf((String) set.getString("IsCommited")))
					+ (Double.valueOf((String) set.getString("OnOrder")));
			map.put("CountedQuantity", Math.max(count, 0.0));
			map.put("Price", set.getString("LastPurPrc"));
			String date = (String) set.getString("DocDate");
			int milTime = set.getInt("DocTime");
			String rawTimestamp = String.format("%04d", milTime);

			DateTimeFormatter inputFormatter = DateTimeFormat.forPattern("HHmm");
			DateTimeFormatter outputFormatter = DateTimeFormat.forPattern("HH:mm");
			DateTime dateTime = inputFormatter.parseDateTime(rawTimestamp);
			String formattedTimestamp = outputFormatter.print(dateTime.getMillis());
			LOG.info("formatted Time: " + formattedTimestamp);
			// System.out.println("Time: " + formattedTimestamp);
			if (date != null) {
				// System.out.println(date);
				// 2019-09-30 00:00:00.000000000
				String time = date.substring(0, 10);
				time = time + " " + formattedTimestamp + ":00.000000000";
				LOG.info("Time: " + time);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSSSSSS");
				Date dateObj = sdf.parse(time);
				Calendar calendar = new GregorianCalendar();
				calendar.setTime(dateObj);
				map.put("calendar", calendar);
			}
			list.add(map);
		}
		ArrayList<HashMap<String, Object>> sortedList = DateTimeSaver.orderByDate(list);

		return sortedList;
	}
}
