package sapb1masterpoll;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class CompareUtils {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap<String, Object> getMostRecetString(String request, String identifier, List<HashMap<String, Object>> list) throws ParseException {
		LOG.info("JAVA.CompareUtils.20: getMostRecentString Called");
		HashMap<String, Object> returnMap = new HashMap();
		returnMap.put("UpdateDate", CurrentTimeSaver.UpdateDate.get(request));
		returnMap.put("UpdateTime", CurrentTimeSaver.UpdateTime.get(request));
		//System.out.println("Date: " + CurrentTimeSaver.UpdateDate);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
		LOG.info("JAVA.CompareUtils.27: Starting iterator to look for most recent date");
		Iterator it = list.iterator();
		while (it.hasNext()) {
			HashMap<String, Object> map = (HashMap<String, Object>) it.next();
			if (!map.get("UpdateDate").toString().equals("null") && !map.get("UpdateTime").toString().equals("null")) {
				Date date1 = sdf.parse(((String) map.get("UpdateDate")) + ' ' + (String) map.get("UpdateTime"));
				Date date2 = sdf.parse(((String) returnMap.get("UpdateDate")) + ' ' + (String) returnMap.get("UpdateTime"));
				if (date1.after(date2)) {
					// System.out.println("Date is AFTER: " + map.get("UpdateDate") +" "+
					// map.get("UpdateTime"));
					returnMap.put("UpdateDate", (String) map.get("UpdateDate"));
					returnMap.put("UpdateTime", (String) map.get("UpdateTime"));
					returnMap.put(identifier, map.get(identifier));
				} else if (date1.equals(date2)) {
					// System.out.println("Date is Equal");
				} else if (date1.before(date2)) {
					// System.out.println("Date is Before: " + map.get("UpdateDate") +" "+
					// map.get("UpdateTime"));
				}
			}
		}
		LOG.info("JAVA.CompareUtils.48: Most Recent found: "
		+returnMap.get("UpdateDate")
		+" "+
		returnMap.get("UpdateTime"))
		;

		return returnMap;
	}
	
	public static ArrayList<HashMap<String,Object>> orderByDate(String identifier, ArrayList<HashMap<String,Object>> list)
	{
		LOG.info("JAVA.CompareUtils.59: Sort List by Date");
		Collections.sort(list, new DateComparator());
		LOG.info("JAVA.CompareUtils.59: Return Sorted List");
		return list;
	}
}

class DateComparator implements Comparator<HashMap<String,Object>>{
    @Override
    public int compare(HashMap<String,Object> obj1, HashMap<String,Object> obj2){
    	
    	if (obj1.containsKey("UpdateDate") && (obj2.containsKey("UpdateDate")) && obj1.containsKey("UpdateTime") && (obj2.containsKey("UpdateTime")))
    	{
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
    		try {
			Date date1 = sdf.parse(((String) obj1.get("UpdateDate")) + ' ' + (String) obj1.get("UpdateTime"));
			Date date2 = sdf.parse(((String) obj2.get("UpdateDate")) + ' ' + (String) obj2.get("UpdateTime"));
			return date1.compareTo(date2);
    		} catch (ParseException e)
    		{
    			return 0;
    		}
    	}
    		else
        return 0;
    }
}
