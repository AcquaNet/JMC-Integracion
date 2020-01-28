package acqua.util;


import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class WorkUtils {
	
	public static List<String> addStringToList(List<String> list, String soc){
		list.add(soc);
		return list;
	}
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	public static JSONObject StringToJson(String str) {
		return new JSONObject(str);
	}
	
	public static String replaceAll (String str) {
		
		return str.replaceAll("\"","'");
	}

	public static HashMap<String,Object> newHashMap(){
		return new HashMap<String,Object>();
	}
}

class CodeComparator implements Comparator<HashMap<String, Object>> {
	String identifier;

	public CodeComparator(String identifier) {
		super();
		this.identifier = identifier;
	}

	@Override
	public int compare(HashMap<String, Object> obj1, HashMap<String, Object> obj2) {

		if (obj1.containsKey(identifier) && (obj2.containsKey(identifier)))
			return ((String) obj1.get(identifier)).compareTo(((String) obj2.get(identifier)));
		else
			return 0;
	}
}
