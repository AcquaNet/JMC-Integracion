package acqua.util.Transform;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.JSONUtil;

public class BuildConteoInventario extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh.log");
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		ArrayList<HashMap<String,Object>> articulos = (ArrayList<HashMap<String, Object>>) message.getPayload();
		String sociedad = (String) message.getInvocationProperty("sociedad");
		String operacion = (String) message.getInvocationProperty("codigo");
		
		HashMap<String,Object> Documento = new HashMap<>();
		ArrayList<Object> CountingLines = new ArrayList<>();
		
		int i = 0;
		for (HashMap<String,Object> map : articulos) {
			HashMap<String,Object> LineMap = new HashMap<>();
			LineMap.put("LineNumber", i);
			LineMap.put("ItemCode", map.get("codigo"));
			LineMap.put("WarehouseCode", "103");
			LineMap.put("Counted", "tYES");
			LineMap.put("CountedQuantity", map.get("cantidad"));
			ArrayList<Object> InventoryCountingBatchNumbers = new ArrayList<>();
				HashMap<String,Object> firstMap = new HashMap<>();
				firstMap.put("BatchNumber", operacion);
				firstMap.put("Quantity", map.get("cantidad"));
				firstMap.put("BaseLineNumber", i);
				InventoryCountingBatchNumbers.add(firstMap);
			LineMap.put("InventoryCountingBatchNumbers", InventoryCountingBatchNumbers);
			
			CountingLines.add(LineMap);
			
			i++;
		}
		
		
		Documento.put("InventoryCountingLines", CountingLines);
		
		return Documento;
	}
	
	
	
}
