package com.acqua.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.springframework.beans.factory.annotation.Value;

public class ProcessStockData implements Callable{
	
	@Value("${sapb1_magento_stock_tipoComprobante}")
	private String tipoComprobante;
	
	
	public String onCall(MuleEventContext eventContext) {
		// =============================================
		// Init Vars 
		// =============================================
		String response = "";
		Boolean missingVariable = false;
		ArrayList<HashMap<String, Object>> updatedItems = null;
		ArrayList<HashMap<String, String>> itemStocks = null;
		ArrayList<HashMap<String, String>> itemDetails = null;
		ArrayList<HashMap<String, String>> itemPrices = null;
		
		
		// =============================================
		// Check for Item Data
		// =============================================
		MuleMessage message = eventContext.getMessage();
		if (message.getInvocationPropertyNames().contains("updatedItems")) {
			updatedItems = message.getInvocationProperty("updatedItems");
		} else {
			missingVariable = true;
		}
		if (message.getInvocationPropertyNames().contains("itemPrices")) {
			itemPrices = message.getInvocationProperty("itemPrices");
		} else {
			missingVariable = true;
		}
		if (message.getInvocationPropertyNames().contains("itemStocks")) {
			itemStocks = message.getInvocationProperty("itemStocks");
		} else {
			missingVariable = true;
		}
		if (message.getInvocationPropertyNames().contains("itemDetails")) {
			itemDetails = message.getInvocationProperty("itemDetails");
		} else {
			missingVariable = true;
		}
		
		if(!missingVariable) {
			// =============================================
			// Getting ItemCode, CreateDate and CreateTime from updatedItems and add secuencial
			// =============================================
			ArrayList<Object> items = new ArrayList<Object>();
			for (int a =0; a<updatedItems.size();a++)
	        {
				
				HashMap<String, Object> item = new HashMap<String, Object>();
	            HashMap<String, Object> tmpData = (HashMap<String, Object>) updatedItems.get(a);
	            
	            item.put("CodArticulo", tmpData.get("ItemCode"));
	            item.put("NumeroComprobante",  a);
	            item.put("FechaComprobante", tmpData.get("CreateDate") + " " + String.valueOf(tmpData.get("CreateTime")));
	            item.put("TipoComprobante", tipoComprobante);
	            
	            // =============================================
    			// Getting item prices from itemPrices
    			// =============================================
	            
	            for (HashMap<String, String> priceLine : itemPrices) {
	            	if (priceLine.get("ItemCode").equals(tmpData.get("ItemCode"))) {
	            		item.put("Precio", priceLine.get("Price"));
	            		break;
	            	}
		        }
	            
	            // =============================================
    			// Getting item stock quantity from itemStocks
    			// =============================================
	            
	            for (HashMap<String, String> stockLine : itemStocks) {
	            	if (stockLine.get("ItemCode").equals(tmpData.get("ItemCode"))) {
	            		item.put("Cantidad", stockLine.get("Quantity"));
	            		break;
	            		
	            	}
		        }
	            
	            
	            for (HashMap<String, String> itemLine : itemDetails)
		        {
	            	// =============================================
	    			// Getting left fields from itemDetails
	    			// =============================================
	            	
	            	if (tmpData.get("ItemCode").equals(itemLine.get("ItemCode"))) {
	            		item.put("DesArticulo", itemLine.get("ItemName"));
	            		item.put("DescColor", itemLine.get("itemColor"));
	            		item.put("EntradaSalida", "E");
	            		item.put("IdDisciplina1", itemLine.get("U_Discip"));
	            		item.put("IdDisciplina2", itemLine.get("U_Subdiscip"));
	            		item.put("DescDisciplina1", itemLine.get("discipName"));
	            		item.put("DescDisciplina2", itemLine.get("subDiscipName"));
	            		item.put("IdTemporada", itemLine.get("U_Tempo"));
	            		item.put("DescTemporada", itemLine.get("tempoName"));
	            		item.put("IdGenero", itemLine.get("U_Genero"));
	            		item.put("DescGenero", itemLine.get("generoName"));
	            		item.put("IdLinea1", itemLine.get("U_Linea"));
	            		item.put("IdLinea2", itemLine.get("U_Sublinea"));
	            		item.put("DescLinea1", itemLine.get("lineaName"));
	            		item.put("DescLinea2", itemLine.get("subLineaName"));
	            		item.put("IdMarca", itemLine.get("U_Marca"));
	            		item.put("DescMarca", itemLine.get("marcaName"));
	            		item.put("IdCategoria1", itemLine.get("ItemCode"));
	            		item.put("IdCategoria2", itemLine.get("U_Subtipo"));
	            		item.put("DescCategoria1", itemLine.get("ItemCode"));
	            		item.put("DescCategoria2", itemLine.get("subTipoName"));
	            		item.put("Habilitado", itemLine.get("validFor"));
	            		item.put("PorcDescuento", 0);
	            		break;
	            	}
	            	
		        }
	            items.add(item);
	        }
			response = JSONUtil.javaListToJSONToString(items);
			
		}
		return response;
	}

}
