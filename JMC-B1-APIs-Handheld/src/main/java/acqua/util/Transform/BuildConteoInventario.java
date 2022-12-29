package acqua.util.Transform;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

public class BuildConteoInventario extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh.log");
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		ArrayList<HashMap<String,Object>> articulos = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("consolidado");
		HashMap<String,Object> articulosHM = new HashMap<String,Object>();
		 
		for(HashMap<String,Object> articulo: articulos)
		{
			articulosHM.put((String) articulo.get("codigo"), (Object) articulo.get("cantidad")); 
		}
		 
		HashMap<String,Object> purchaseOrder = (HashMap<String, Object>) message.getInvocationProperty("purchaseOrder");
		String sociedad = (String) message.getInvocationProperty("sociedad");
		String codigo = (String) message.getInvocationProperty("codigo");
		String puntoventa = (String) message.getInvocationProperty("puntoventa");
		String letra = (String) message.getInvocationProperty("letra");
		String foliodesde = (String) message.getInvocationProperty("foliodesde");
		String foliohasta = (String) message.getInvocationProperty("foliohasta"); 
  
		HashMap<String,Object> documento = new HashMap<>(); 
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String dateToday = df.format(new Date());
		
		documento.put("odata.etag", purchaseOrder.get("odata.metadata"));
		
		documento.put("DocumentLines", new ArrayList<HashMap<String,Object>>());
		
		ArrayList<HashMap<String,Object>> values = (ArrayList<HashMap<String, Object>>) purchaseOrder.get("value");
		
		if(!values.isEmpty())
		{
			
			HashMap<String, Object> valor = values.get(0);
			 
			for (Entry<String, Object> val : valor.entrySet()) {
				
				String keyValue = val.getKey();
				Object vale = val.getValue();
				
				if(keyValue.equals("DocumentLines"))
				{
					ArrayList<HashMap<String,Object>> lineas =  (ArrayList<HashMap<String, Object>>) vale;
					 
					for(HashMap<String,Object> linea: lineas)
					{
						if(articulosHM.containsKey(linea.get("ItemCode"))){
						
							HashMap<String,Object> nuevaLinea = new HashMap<>();
							
							for (Entry<String, Object> lin : linea.entrySet()) {
								
								String keyLineaValue = lin.getKey();
								Object lineaValue = lin.getValue();
								
								nuevaLinea.put(keyLineaValue, lineaValue);
								
							}
							
							// Calcular Excedente de lo solicitado
							//
							
							Long qtyToSplit = 0L;
							
							if(nuevaLinea.containsKey("RemainingOpenQuantity"))
							{
								Long qtyRemaningOpenQty = (Long) nuevaLinea.get("RemainingOpenQuantity");
								Long qty = (Long) articulosHM.get(linea.get("ItemCode");
								if(qty>qtyRemaningOpenQty)
								{
									qtyToSplit = qty - qtyRemaningOpenQty;
									System.out.println("Qty pendiente a procesar " + qtyToSplit);
								}
							}
							
							
							nuevaLinea.replace("BaseLine", nuevaLinea.get("LineNum"));
							nuevaLinea.replace("BaseOpenQuantity", articulosHM.get(linea.get("ItemCode")));
							nuevaLinea.replace("DocEntry", valor.get("DocEntry"));
							nuevaLinea.replace("BaseOpenQuantity", articulosHM.get(linea.get("ItemCode")));
							nuevaLinea.replace("ActualDeliveryDate", dateToday);
							nuevaLinea.replace("BaseType", 22);
							nuevaLinea.replace("BaseEntry", valor.get("DocEntry"));
							
							// Cantidades Adicionales
							nuevaLinea.replace("Quantity", articulosHM.get(linea.get("ItemCode"))); 
							nuevaLinea.replace("PackageQuantity", articulosHM.get(linea.get("ItemCode"))); 
							nuevaLinea.replace("RemainingOpenQuantity", articulosHM.get(linea.get("ItemCode"))); 
							nuevaLinea.replace("InventoryQuantity", articulosHM.get(linea.get("ItemCode"))); 
							if(nuevaLinea.containsKey("RemainingOpenInventoryQuantity"))
							{
								nuevaLinea.replace("RemainingOpenInventoryQuantity", articulosHM.get(linea.get("ItemCode"))); 
							}
							
							
							// Batch Number
							
							HashMap<String,Object> batchNumber = new HashMap<>();
							
							batchNumber.put("BaseLineNumber", nuevaLinea.get("LineNum"));
							batchNumber.put("BatchNumber", valor.get("NumAtCard"));
							batchNumber.put("Quantity", articulosHM.get(linea.get("ItemCode")));
							String systemSerialNumber =  getSystemSerialNumber(message, valor.get("DocNum").toString());							
							batchNumber.put("SystemSerialNumber", systemSerialNumber);
							nuevaLinea.replace("BatchNumbers", new ArrayList<HashMap<String,Object>>());
							((ArrayList<HashMap<String,Object>>) nuevaLinea.get("BatchNumbers")).add(batchNumber);

							
							// DocumentLinesBinAllocations
							
							HashMap<String,Object> documentLinesBinAllocations = new HashMap<>();
							documentLinesBinAllocations.put("BaseLineNumber", nuevaLinea.get("LineNum"));
							documentLinesBinAllocations.put("SerialAndBatchNumbersBaseLine", 0);
							documentLinesBinAllocations.put("Quantity", articulosHM.get(linea.get("ItemCode")));
							String binAbsEntry = getBinAbsEntry(message, linea.get("WarehouseCode").toString());
							documentLinesBinAllocations.put("BinAbsEntry", binAbsEntry);
							
							nuevaLinea.put("DocumentLinesBinAllocations", new ArrayList<HashMap<String,Object>>());
							((ArrayList<HashMap<String,Object>>) nuevaLinea.get("DocumentLinesBinAllocations")).add(documentLinesBinAllocations);
							
							 
							((ArrayList<HashMap<String,Object>>) documento.get("DocumentLines")).add(nuevaLinea);
							
							articulosHM.remove(linea.get("ItemCode")); // Remover el articulo procesado
							
						} 
						
					}
					
					// Procesar Lineas notificadas que no existen en la orden.
					
					for(Entry<String, Object> lineasPendientes: articulosHM.entrySet())
					{
						System.out.println("Codigo pendiente a procesar " + lineasPendientes.getKey());
						System.out.println("                   Cantidad " + lineasPendientes.getValue());
					}
					
					
				} else
				{
					documento.put(keyValue, vale);
				}
				  
			}
			
			documento.replace("Comments", "Basado en Pedidos " + codigo);
			documento.replace("U_NroCompEsp", documento.get("NumAtCard"));
			documento.remove("Reference1");
			documento.remove("JournalMemo");
			documento.remove("DocObjectCode");
			documento.remove("DocTotal");
			documento.replace("PointOfIssueCode",puntoventa);
			documento.replace("Letter",letra);
			documento.replace("FolioNumberFrom",foliodesde);
			documento.replace("FolioNumberTo",foliohasta);
			documento.replace("DocDate",dateToday);
			documento.replace("DocDueDate",dateToday);
			documento.replace("TaxDate",dateToday);
			documento.replace("CreationDate",dateToday);
			documento.replace("UpdateDate",dateToday);
			documento.replace("Series",17);
			documento.remove("FinancialPeriod");
			documento.replace("WareHouseUpdateType","dwh_Stock"); 
			
			((HashMap<String, Object>) documento.get("TaxExtension")).replace("NFRef", "Basado en Pedidos " + codigo);
			
			
		}
		  
		return documento;
	}
	
	private String getSystemSerialNumber(MuleMessage message, String docNum){
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");		
		String sociedad = message.getInvocationProperty("sociedad");

		ODBCManager manager = new ODBCManager(user, password, connectionString);
		Object connect = manager.connect();
		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			System.out.println("Fallo conexion a BD");
			return null;
		}
		try {
			manager.createStatement();
			String Query = "SELECT T0.\"NumAtCard\" FROM "+sociedad+".OPOR T0 " + " WHERE T0.\"DocNum\" = '"+ docNum +"'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);
			HashMap<String, Object> queryResult = parseQuerySystemSerialNumber(querySet);
			LOG.info("Parsing done!");
			return (String) queryResult.get("numAtCard");
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				System.out.println("Fallo sql");
				return null;
			}
		}				
		return null;
	}

	public HashMap<String, Object> parseQuerySystemSerialNumber(ResultSet set) throws SQLException {
		HashMap<String, Object> answer = new HashMap<>();
		while (set.next() != false) {
			answer.put("numAtCard", (set.getString("NumAtCard")));
		}
		return answer;
	}	
	
	private String getBinAbsEntry(MuleMessage message, String whsCode){
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");		
		String sociedad = message.getInvocationProperty("sociedad");

		ODBCManager manager = new ODBCManager(user, password, connectionString);
		Object connect = manager.connect();
		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			System.out.println("Fallo conexion a BD");
			return null;
		}
		try {
			manager.createStatement();
			String Query = "SELECT T0.\"AbsEntry\" FROM "+sociedad+".OBIN T0 " + " WHERE T0.\"WhsCode\" = '" + whsCode + "' AND T0.\"SysBin\" = 'Y' ";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);
			HashMap<String, Object> queryResult = parseQueryBinsAbsEntry(querySet);
			LOG.info("Parsing done!");
			return (String) queryResult.get("absEntry");
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				System.out.println("Fallo sql");
				return null;
			}
		}				
		return null;
	}	
	
	public HashMap<String, Object> parseQueryBinsAbsEntry(ResultSet set) throws SQLException {
		HashMap<String, Object> answer = new HashMap<>();
		while (set.next() != false) {
			answer.put("absEntry", (set.getString("AbsEntry")));
		}
		return answer;
	}	
}