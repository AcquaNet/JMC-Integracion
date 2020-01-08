package acqua.util.Transform;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.JSONUtil;

public class BuildRecuentoInventario extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh.log");
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		
		
		int AbsEntry = Integer.valueOf(message.getInvocationProperty("AbsEntry"));
		
		HashMap<String,Object> inputData = message.getInvocationProperty("inputData");
		

		ArrayList<HashMap<String,Object>> existentes = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("existentes");
		System.out.println(JSONUtil.javaListToJSONToString((ArrayList<Object>) message.getInvocationProperty("existentes")));
		System.out.println(existentes.size());
		int i = 0;
		for (HashMap<String,Object> map : existentes) {
			if ( (int) map.get("LineNumber") > i) {
				i = (int) map.get("LineNumber");
			}
		}

		
		ArrayList<HashMap<String,Object>> arrayInput = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("articulos");
		for (HashMap<String,Object> map : arrayInput) {
			i++;
			Boolean exists = false;
			int a = 0;
			for (HashMap<String,Object> existMap : existentes) {		
				if (existMap.get("ItemCode").equals(map.get("codigo")))
				{
					exists = true;
					break;
				}
				a++;
			}
			if (!exists) {
			HashMap<String,Object> newMap = new HashMap<>();
			newMap.put("LineNumber", i);
			newMap.put("ItemCode", map.get("codigo"));
			newMap.put("UoMCode", map.get("UoMCode"));
			newMap.put("Counted", "tYES");
			newMap.put("CountedQuantity", (Double) map.get("cantidad"));
			newMap.put("UoMCountedQuantity", (Double) map.get("cantidad"));
			newMap.put("WarehouseCode", inputData.get("deposito"));
			newMap.put("BinEntry", AbsEntry);
			existentes.add(newMap);
			}
			else
			{
				HashMap<String,Object> oldMap = existentes.get(a);
				oldMap.put("CountedQuantity", (Double) oldMap.get("CountedQuantity") +(Double) map.get("cantidad"));
				existentes.remove(a);
				existentes.add(a, oldMap);
			}
		}
		
		
		HashMap<String,Object> DocumentoRecuentoInventario = new HashMap<>();
		DocumentoRecuentoInventario.put("InventoryCountingLines", existentes);
		
		return DocumentoRecuentoInventario;
	}
	
	
	
}
