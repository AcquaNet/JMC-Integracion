package acqua.util;

import java.util.ArrayList;
import java.util.HashMap;

public class CurvasUtils {
	
	public static String getItemCodes (ArrayList<HashMap<String, Object>> itemList) {
		
        String itemCodes = "";
        
        for (HashMap<String, Object> item : itemList) {
            
            itemCodes += item.get("itemCode") + ",";
            
        }
        
        return itemCodes.substring(0, itemCodes.length() - 1);
        

	}

}
