package com.acqua.tools;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.mule.util.CaseInsensitiveHashMap;



public class RemoveResultDupes {
	
	public static ArrayList<CaseInsensitiveHashMap> removeDupes (ArrayList<CaseInsensitiveHashMap> payload){
		ArrayList<CaseInsensitiveHashMap> result = new ArrayList<CaseInsensitiveHashMap>();
		
		for (CaseInsensitiveHashMap item : payload) {
			
			boolean add = true;
			for (CaseInsensitiveHashMap savedItem : result) {
				if (savedItem.get("ItemCode").equals(item.get("ItemCode"))) {
					add = false;
					break;
				}
			}
			if (add) {
				result.add(item);
			}
		}
		
		
		return result;
	}
	
	public static ArrayList<CaseInsensitiveHashMap> removeDupesPrice (ArrayList<CaseInsensitiveHashMap> payload){
		ArrayList<CaseInsensitiveHashMap> result = new ArrayList<CaseInsensitiveHashMap>();
		try {
		for (CaseInsensitiveHashMap item : payload) {
			BigDecimal itemPrice = (BigDecimal) item.get("Price");
			if (itemPrice == null ? false : itemPrice.compareTo(BigDecimal.ZERO) > 0)  {
				boolean add = true;
				for (CaseInsensitiveHashMap savedItem : result) {
					if (savedItem.get("ItemCode").equals(item.get("ItemCode"))) {
						add = false;
						break;
					}
				}
				if (add) {
					result.add(item);
				}
			}
			
		}
		} catch (Exception e) {
			System.out.println(e);
		}
		
		
		return result;
	}

}
