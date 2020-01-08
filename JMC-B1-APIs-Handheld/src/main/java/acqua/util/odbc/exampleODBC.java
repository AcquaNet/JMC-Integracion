package acqua.util.odbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.DateTimeSaver;
import acqua.util.ODBCManager;

public class exampleODBC extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		
		//String connectionString = message.getInvocationProperty("DBConnection");
		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Object connect = manager.connect();
		
		if (!connect.getClass().equals(Connection.class)) {
			return "Error Connecting to DB. "+(String) connect;
		}
		
		System.out.println("Connection to HANA successful!");
		try {
			// Create a statement to call
			manager.createStatement();
			// Decide which database we're hitting
			String destination = message.getInvocationProperty("Destination");
			// Check which Items are Inventory enabled
			ResultSet stockEnabled = manager
					.executeQuery("SELECT \"ItemCode\",\"InvntItem\" FROM " + destination + ".OITM");
			// Save a HashMap of all the Items that are inventory enabled
			HashMap<String, Boolean> invItems = parseInventoryResults(stockEnabled);
			// Get the last updated Date YYYY-MM-DD
			String updateDate = message.getInvocationProperty("updateDate");
			// Get all Item Stocks from DB
			String str = 
					"SELECT \"ItemCode\", \"WhsCode\",\"OnHand\", \"IsCommited\", \"OnOrder\",\"createDate\", \"updateDate\""
							+ " FROM " + destination + ".OITW" + " WHERE \"WhsCode\" = '01' AND (\"updateDate\" >= '" + updateDate + "'"
							+ " OR \"createDate\" >= '" + updateDate + "')";
			
			ResultSet Items = manager.executeQuery(str);
			
			// Parse results as array list ordered by date from oldest (top) to newest
			// (bottom)
			ArrayList<HashMap<String, Object>> results = parseItemSelect(Items);
			// Create a hashmap to hold the arraylist
			LOG.info("Total results: "+results.size());
			//System.out.println(message);
			LOG.info("Result returned!");
			//LOG.info(""+StringToJSON.javaToJSONToString(result));
			
			List<List<HashMap<String, Object>>> arraySplit = splitArray(results, 1000);
			ArrayList<String> Documents = new ArrayList<String>();
			for (List<HashMap<String, Object>> array : arraySplit) {
				Documents.add(arrayToDocument(array, destination, invItems).toString());
			}
			
			return Documents;
		} catch (SQLException | NumberFormatException | ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static JSONObject arrayToDocument(List<HashMap<String, Object>> inputArray, String destination, HashMap<String, Boolean> invItems) {
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray();
		int i = 1;
		for (HashMap<String, Object> map : inputArray) {
			// System.out.println(((Calendar) map.get("calendar")).getTime());
			JSONObject jsonMap = new JSONObject();
			jsonMap.put("LineNumber", i);
			jsonMap.put("ItemCode", (String) map.get("ItemCode"));
			
			jsonMap.put("WarehouseCode", destination+"_"+(String) map.get("WharehouseCode"));
			double count = 0;
			count = (Double) map.get("CountedQuantity");
			jsonMap.put("CountedQuantity", count);
			if (invItems.get((String) map.get("ItemCode"))) {
				array.put(jsonMap);
			}
			i++;
		}
		obj.put("InventoryPostingLines", array);
		return obj;
	}

	public static List<List<HashMap<String, Object>>> splitArray(ArrayList<HashMap<String, Object>> arrayToSplit, int chunkSize){
	    if(chunkSize<=0){
	        return null;  // just in case :)
	    }
	    //editado de https://stackoverflow.com/questions/27857011/how-to-split-a-string-array-into-small-chunk-arrays-in-java
	    // first we have to check if the array can be split in multiple 
	    // arrays of equal 'chunk' size
	    int rest = arrayToSplit.size() % chunkSize;  // if rest>0 then our last array will have less elements than the others 
	    // then we check in how many arrays we can split our input array
	    int chunks = arrayToSplit.size() / chunkSize + (rest > 0 ? 1 : 0); // we may have to add an additional array for the 'rest'
	    // now we know how many arrays we need and create our result array
	    List<List<HashMap<String, Object>>> arrays = new ArrayList<List<HashMap<String, Object>>>();
	    // we create our resulting arrays by copying the corresponding 
	    // part from the input array. If we have a rest (rest>0), then
	    // the last array will have less elements than the others. This 
	    // needs to be handled separately, so we iterate 1 times less.
	    for(int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++){
	        // this copies 'chunk' times 'chunkSize' elements into a new array
	    	List<HashMap<String, Object>> array = arrayToSplit.subList(i * chunkSize, i * chunkSize + chunkSize);
	    	arrays.add(array);
	    }
	    if(rest > 0){ // only when we have a rest
	        // we copy the remaining elements into the last chunk
	        //arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest);
	    	List<HashMap<String, Object>> array = arrayToSplit.subList((chunks - 1) * chunkSize,(chunks - 1) * chunkSize + rest);
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
			count = (Double.valueOf((String) set.getString("OnHand"))) - (Double.valueOf((String) set.getString("IsCommited")))
					+ (Double.valueOf((String) set.getString("OnOrder")));
			map.put("CountedQuantity", count);
			String date = (String) set.getString("updateDate");
			if (date == null) {
				date = (String) set.getString("createDate");
			}
			if (date != null) {
				// System.out.println(date);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSSSSSSS");
				Date dateObj = sdf.parse(date);
				Calendar calendar = new GregorianCalendar();
				calendar.setTime(dateObj);
				//map.put("calendar", calendar);
			}
			list.add(map);
		}
		ArrayList<HashMap<String,Object>> sortedList = DateTimeSaver.orderByDate(list);
		
		return sortedList;
	}
}
