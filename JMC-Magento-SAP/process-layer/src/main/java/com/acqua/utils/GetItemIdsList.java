package com.acqua.utils;

import java.util.ArrayList;
import java.util.HashMap;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;

public class GetItemIdsList implements Callable {
	public Object onCall(MuleEventContext eventContext) throws Exception {
		String result = "";
		MuleMessage message = eventContext.getMessage();
		if (message.getInvocationPropertyNames().contains("updatedItems")) {
			ArrayList<HashMap<String, String>> itemList = message.getInvocationProperty("updatedItems");
			for (int a =0; a<itemList.size();a++)
	        {
	            HashMap<String, String> tmpData = (HashMap<String, String>) itemList.get(a);
	                String hmData = (String) tmpData.get("ItemCode");
	                if (result == "") {
	                	result = hmData;
	                }else
	                {
	                	result += "," + hmData;
	                }
	            

	        }
			message.setInvocationProperty("idList", result);
		} else {
			result = "There are no items to process";
		}
		return message;
	}
	
}
