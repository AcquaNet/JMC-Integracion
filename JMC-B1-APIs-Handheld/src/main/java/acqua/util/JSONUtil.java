package acqua.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONUtil {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	// Actual utility methods
	
	public static boolean saveRequest(String request) throws IOException {
		LOG.info("JAVA.StringToJSON.22: javaToJSONToString Called");
		String path = "/tmp/request.log";
		FileUtils.writeStringToFile(new File(path), request, StandardCharsets.UTF_8); 
		
		return true;
	}
	
	
	// Input a HashMap<String,Object> and return a JSON String
	public static String javaToJSONToString(HashMap<String,Object> map) {
		LOG.info("JAVA.StringToJSON.22: javaToJSONToString Called");
		Gson gson =  new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(map); 
		return json;
	}
	
	public static String javaToJSONToStringWithNull(HashMap<String,Object> map) {
		LOG.info("JAVA.StringToJSON.22: javaToJSONToStringWithNull Called"); 
		Gson gson =  new GsonBuilder().serializeNulls().setPrettyPrinting().create(); 
		String json = gson.toJson(map); 
		return json;
	} 
	
	// Input a ArrayList and return a JSON String
	public static String javaListToJSONToString(ArrayList<Object> list) {
		LOG.info("JAVA.StringToJSON.22: javaToJSONToString Called");
		Gson gson =  new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(list); 
		return json;
	}
	
	// Input a JSON String and return a Map
	public static Map<String, Object> stringToMap(String str){
		if (str.equals("")) {
			return new HashMap<String,Object>();
		} 
		LOG.info("JAVA.StringToJSON.32: stringToMap Called");

		return jsonToMap(new JSONObject(str));
	}
	
	
	// Private in-class methods for processing from here on
	
	private static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
	    Map<String, Object> retMap = new HashMap<String, Object>();
	    LOG.info("JAVA.StringToJSON.38: jsonToMap Called - Calls toMap");
	    if(json != JSONObject.NULL) {
	        retMap = toMap(json);
	    }
	    return retMap;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> toMap(JSONObject object) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();

	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);

	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }
	        
	        else if(value.getClass().getName().equals("org.json.JSONObject$Null")) {
	            value = null;
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        } 
	        if(value instanceof java.lang.String)
	        { 
	        	value =  ((java.lang.String) value).replaceAll("\\n", " "); 
	        	value =  ((java.lang.String) value).replaceAll("\\r", " "); 
	        }
	        map.put(key, value);
	    }
	    return map;
	}

	private static List<Object> toList(JSONArray array) throws JSONException {
	    List<Object> list = new ArrayList<Object>();
	    for(int i = 0; i < array.length(); i++) {
	        Object value = array.get(i);
	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        list.add(value);
	    }
	    return list;
	}
}
