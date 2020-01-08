package acqua.util.Transform;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.JSONUtil;

public class BuildTransferencia extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh.log");
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		

		ArrayList<HashMap<String,Object>> articulos = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("articulos");
		//ArrayList<HashMap<String,Object>> articulos =  (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("articulos");
		String almacenOrigen = message.getInvocationProperty("almacenOrigen");
		String almacenDestino = message.getInvocationProperty("almacenDestino");
		String origen = message.getInvocationProperty("origen");
		String destino = message.getInvocationProperty("destino");
		HashMap<String,HashMap<String,Object>> lotes = message.getInvocationProperty("lotes");
		String sociedad = (String)  message.getInvocationProperty("sociedad");
		//System.out.println(articulos.size());
		int i = 0;

		
		ArrayList<HashMap<String,Object>> result = new ArrayList<HashMap<String, Object>>();
		for (HashMap<String,Object> map : articulos) {
			HashMap<String,Object> newMap = new HashMap<>();
			newMap.put("LineNum", i);
			newMap.put("ItemCode", map.get("codigo"));
			//newMap.put("UoMCode", map.get("UoMCode"));
			newMap.put("Quantity", (Double) map.get("cantidad"));
			newMap.put("FromWarehouseCode", almacenOrigen);
			newMap.put("WarehouseCode", almacenDestino);
			
			// Asignación de Lotes
			Double cantidad = (Double) map.get("cantidad");
			ArrayList<HashMap<String,Object>> BatchNumbers = new ArrayList<>();
			
			
			HashMap<String,Object> lotesArticulo = lotes.get(map.get("codigo"));
			
			for (String distNumber : lotesArticulo.keySet()) {
				Double disponible = Double.valueOf((String) lotesArticulo.get(distNumber));
				HashMap<String,Object> batchLine = new HashMap<>();
				if (disponible > cantidad) {
					batchLine.put("BatchNumber", distNumber);
					batchLine.put("Quantity", cantidad);
					batchLine.put("BaseLineNumber", i);
					BatchNumbers.add(batchLine);
					cantidad = 0.0;
					break;
				}
				else if (disponible == cantidad) {
					batchLine.put("BatchNumber", distNumber);
					batchLine.put("Quantity", cantidad);
					batchLine.put("BaseLineNumber", i);
					BatchNumbers.add(batchLine);
					cantidad = 0.0;
					break;
				}
				else {
					batchLine.put("BatchNumber", distNumber);
					batchLine.put("Quantity", disponible);
					batchLine.put("BaseLineNumber", i);
					BatchNumbers.add(batchLine);
					cantidad = cantidad - disponible;
				}
			}
			if (cantidad > 0.0) {
			LOG.error("Not enough stock for Item '"+map.get("codigo")+"' on bin "+sociedad+"."+origen+" . Missing: "+cantidad);
			}
			else
			{
				
				ArrayList<HashMap<String,Object>> binAllocation = new ArrayList<>();
				int batchLineCount = 0;
				for (HashMap<String,Object> batchLine : BatchNumbers) {
				HashMap<String,Object> allocationOrig = new HashMap<String,Object>();
				HashMap<String,Object> allocationDest = new HashMap<String,Object>();
				
				allocationOrig.put("BaseLineNumber", i);
				allocationDest.put("BaseLineNumber", i);
				
				allocationOrig.put("SerialAndBatchNumbersBaseLine", batchLineCount);
				allocationDest.put("SerialAndBatchNumbersBaseLine", batchLineCount);
				
				allocationOrig.put("BinActionType", "batFromWarehouse");
				allocationDest.put("BinActionType", "batToWarehouse");
				
				allocationOrig.put("AllowNegativeQuantity", "tYES");
				allocationDest.put("AllowNegativeQuantity", "tYES");
				
				allocationOrig.put("BinAbsEntry", origen);
				allocationDest.put("BinAbsEntry", destino);
				
				allocationOrig.put("Quantity", batchLine.get("Quantity"));
				allocationDest.put("Quantity", batchLine.get("Quantity"));
				
				
				binAllocation.add(allocationOrig);
				binAllocation.add(allocationDest);
				
				batchLineCount++;
				}
				
				newMap.put("StockTransferLinesBinAllocations", binAllocation);
				newMap.put("BatchNumbers", BatchNumbers);
				result.add(newMap);
			}
			
			i++;
		}
		
//        "StockTransferLinesBinAllocations": [
//                                             {
//                                                 "BinAbsEntry": 5,
//                                                 "Quantity": 2,
//                                                 "AllowNegativeQuantity": "tNO",
//                                                 "SerialAndBatchNumbersBaseLine": -1,
//                                                 "BinActionType": "batFromWarehouse",
//                                                 "BaseLineNumber": 0
//                                             },
//                                             {
//                                                 "BinAbsEntry": 17278,
//                                                 "Quantity": 2,
//                                                 "AllowNegativeQuantity": "tNO",
//                                                 "SerialAndBatchNumbersBaseLine": -1,
//                                                 "BinActionType": "batToWarehouse",
//                                                 "BaseLineNumber": 0
//                                             }
//                                         ]
		
		HashMap<String,Object> DocumentoRecuentoInventario = new HashMap<>();
		DocumentoRecuentoInventario.put("StockTransferLines", result);
		DocumentoRecuentoInventario.put("PointOfIssueCode", "9999");
		
		return DocumentoRecuentoInventario;
	}
	
	
	
}
