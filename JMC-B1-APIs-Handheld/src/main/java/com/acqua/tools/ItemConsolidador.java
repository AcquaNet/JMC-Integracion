package com.acqua.tools;

import java.util.HashMap;

public class ItemConsolidador {

		private static HashMap<String, Integer> items = new HashMap<String, Integer>();
		private static HashMap<String, Integer> itemsCargados = new HashMap<String, Integer>();
		
		public static Boolean checkItems(String itemId) {
			Boolean result = false;
			if(itemsCargados.containsKey(itemId)){
				result = true;
			}else
			{
				itemsCargados.put(itemId, 1);
			}
			return result;
		}
		
		public static Boolean resetCheckItems() {
			if(itemsCargados.size() > 0) {
				itemsCargados.clear();
			}
			return true;
		}
		
		public static void addItem(String itemId, Integer desc){
			if(desc == null){
				desc = 0;
			}
			if(items.containsKey(itemId)){
				if (desc != 1){
				Integer cant = items.get(itemId)+1;
				items.put(itemId, cant);
				Integer cant2 = items.get(itemId+"i")+1;
				items.put(itemId+"i", cant2);
				}
				else
				{
					Integer cant = items.get(itemId)-1;
					items.put(itemId, cant);
					Integer cant2 = items.get(itemId+"i")-1;
					items.put(itemId+"i", cant2);
				}
			}else
			{
				if (desc != 1){
				items.put(itemId, 1);
				items.put(itemId+"i", 1);
				}else
				{
					items.put(itemId, 0);
					items.put(itemId+"i", 0);
				}
			}
		}
		
		public static HashMap<String, Integer> getItems(){
			return items;
		}
		
		public static Integer descItems(String itemId, String cantOC){
			Integer result = 0;
			
			if(items.containsKey(itemId)){
			
			Integer cant = items.get(itemId);
			Integer dif = cant - Integer.parseInt(cantOC);
			
			if(dif >= 0){
				items.put(itemId, dif);
				result = Integer.parseInt(cantOC);
			}else
			{
				items.put(itemId, 0);
				result = cant;
			}
			}
			return result;
		}
		public static String actLineStatus(String itemCode, String lineStatus, Integer remainingOQ, Integer remainingOIQ)
		{
			String result = lineStatus;
			if(lineStatus != "bost_Close"){
			if(items.containsKey(itemCode+"i")){
				Integer cant2 = items.get(itemCode+"i");
				if(cant2 >= remainingOQ){
					result = "bost_Close";	
				}else
				{
					result = "bost_Open";
					items.put("Open", 1);
				}
			}else
			{
				
					items.put("Open", 1);
				
			}
			}else
			{
				items.put("Open", 0);
			}
			return result;
		}
		
		public static Integer actRemainingOQ(String itemCode, Integer remainingOQ, String lineStatus){
			Integer result = 0;
			if(lineStatus != "bost_Close"){
			if(items.containsKey(itemCode+"i")){
				Integer cant = items.get(itemCode+"i");
				if(cant > remainingOQ){
					result = 0;
					Integer proc = cant - remainingOQ;
					items.put(itemCode+"i", proc);
				}else
				{
					Integer proc = remainingOQ - cant;
					result = proc;
					items.put(itemCode+"i", 0);
				}
			}else
			{
				result = remainingOQ;
			}
			}
			return result;
		}
		public static String getDocumentStatus(){
			String result = "bost_Close";
			
			if(items.containsKey("Open")){
			Integer proc = items.get("Open");
			if(proc == 1){
			result = "bost_Open";
			}
			}
			
			return result;
		}
		public static Boolean resetOpenOC(){
			
			if(items.containsKey("Open")){
				Integer proc = items.get("Open");
				if(proc == 1){
					items.put("Open", 0);
				}
			}else
			{
				items.put("Open", 0);
			}
			
			return true;
		}
		
		public static Boolean resetItems(){
			if(items != null){
			if(items.size() > 0){
				items.clear();
			}
			}
			return true;
		}
		
}
