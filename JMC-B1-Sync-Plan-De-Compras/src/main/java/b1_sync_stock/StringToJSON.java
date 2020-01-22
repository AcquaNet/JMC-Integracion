package b1_sync_stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StringToJSON {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	public static JSONObject javaToJSON(HashMap<String,Object> map) {
		LOG.info("JAVA.StringToJSON.17: javaToJSON Called");
		return new JSONObject(map);
	}
	
	public static String javaToJSONToString(HashMap<String,Object> map) {
		LOG.info("JAVA.StringToJSON.22: javaToJSONToString Called");
		return new JSONObject(map).toString();
	}
	
	public static JSONArray listToJSON(List<HashMap<String,Object>> list) {
		LOG.info("JAVA.StringToJSON.27: listToJSON Called");
		return new JSONArray(list);
	}
	
	public static Map<String, Object> stringToMap(String str){
		LOG.info("JAVA.StringToJSON.32: stringToMap Called");
		return jsonToMap(new JSONObject(str));
	}
	
	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
	    Map<String, Object> retMap = new HashMap<String, Object>();
	    LOG.info("JAVA.StringToJSON.38: jsonToMap Called - Calls toMap");
	    if(json != JSONObject.NULL) {
	        retMap = toMap(json);
	    }
	    return retMap;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
	    Map<String, Object> map = new HashMap<String, Object>();

	    Iterator<String> keysItr = object.keys();
	    while(keysItr.hasNext()) {
	        String key = keysItr.next();
	        Object value = object.get(key);

	        if(value instanceof JSONArray) {
	            value = toList((JSONArray) value);
	        }

	        else if(value instanceof JSONObject) {
	            value = toMap((JSONObject) value);
	        }
	        map.put(key, value);
	    }
	    return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
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
