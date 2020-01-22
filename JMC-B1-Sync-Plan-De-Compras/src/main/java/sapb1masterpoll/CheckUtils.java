package sapb1masterpoll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class CheckUtils {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean containsNull(List<HashMap<String, Object>> list ) {
		LOG.info("JAVA.CheckUtils.15: Checking for NULL Date");
			Iterator it = list.iterator();
			while (it.hasNext()) {
				HashMap<String,Object> map = (HashMap<String, Object>) it.next();
				if (map.get("UpdateTime").toString().equals("null") || map.get("UpdateDate").toString().equals("null")) {
					LOG.info("JAVA.CheckUtils.20: Null Date element found");
					return true;
				}
			}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<HashMap<String, Object>> getNulls(List<HashMap<String, Object>> list ){
		LOG.info("JAVA.CheckUtils.29: Get Nulls start");
		List<HashMap<String,Object>> returnList = new ArrayList<HashMap<String,Object>>();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			HashMap<String,Object> map = (HashMap<String, Object>) it.next();
			if (map.get("UpdateTime").toString().equals("null") || map.get("UpdateDate").toString().equals("null")) {
				
				returnList.add(map);
			}
		}
		LOG.info("JAVA.CheckUtils.39: All Nulls assembled, Return them to Mule");
		return returnList;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<HashMap<String, Object>> getNotNulls(List<HashMap<String, Object>> list ){
		LOG.info("JAVA.CheckUtils.45: getNotNulls Called");
		List<HashMap<String,Object>> returnList = new ArrayList<HashMap<String,Object>>();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			HashMap<String,Object> map = (HashMap<String, Object>) it.next();
			if (!map.get("UpdateTime").toString().equals("null") && !map.get("UpdateDate").toString().equals("null")) {
				returnList.add(map);
			}
		}
		LOG.info("JAVA.CheckUtils.54: Not Null Date elements Returned");
		return returnList;
	}
}
