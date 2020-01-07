package com.acqua.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

public class ConcatenateStock implements Callable{
	public Object onCall(MuleEventContext eventContext) throws Exception{
		MuleMessage message = eventContext.getMessage();
		
		Boolean missingVariable = false;
		ArrayList<HashMap<String, Object>> itemStocks = null;
		ArrayList<HashMap<String, Object>> stockResults = new ArrayList<HashMap<String, Object>>();
		
		if (message.getInvocationPropertyNames().contains("itemStocks")) {
			itemStocks = message.getInvocationProperty("itemStocks");
		} else {
			missingVariable = true;
		}
		
		if (!missingVariable) {
			for (HashMap<String, Object> stockLine : itemStocks) {
				Boolean exists = false;
				for (HashMap<String, Object> resultLine : stockResults) {
					if (resultLine.get("ItemCode").equals(stockLine.get("ItemCode"))) {
						Double savedQuantity = (Double) resultLine.get("Quantity");
						resultLine.put("Quantity", savedQuantity + (Double) stockLine.get("OnHand") + (Double) stockLine.get("OnOrder") - (Double) stockLine.get("IsCommited"));
						exists = true;
						break;
					}
				}
				if (!exists) {
					HashMap<String, Object> newStockLine = new HashMap<String, Object>();
					newStockLine.put("ItemCode", stockLine.get("ItemCode"));
					newStockLine.put("Quantity", (Double) stockLine.get("OnHand") + (Double) stockLine.get("OnOrder") - (Double) stockLine.get("IsCommited"));
					stockResults.add(newStockLine);
				}
				
			}
			message.setInvocationProperty("itemStocks", stockResults);
		}
		
		return message;
		
	}

}
