package b1_sync_stock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class CompareUtils {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap<String, Object> getMostRecetString(String request, String identifier, List<HashMap<String, Object>> list) throws ParseException {
		HashMap<String, Object> returnMap = new HashMap();
		returnMap.put("UpdateDate", "");
		returnMap.put("UpdateTime","");
		//System.out.println("Date: " + CurrentTimeSaver.UpdateDate);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
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

		return returnMap;
	}
	
	public static ArrayList<HashMap<String,Object>> orderByDate(String identifier, ArrayList<HashMap<String,Object>> list)
	{
		Collections.sort(list, new DateComparator());
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
