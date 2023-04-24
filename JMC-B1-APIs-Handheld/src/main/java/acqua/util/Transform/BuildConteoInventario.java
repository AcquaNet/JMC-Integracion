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
		 
		//String sociedad = (String) message.getInvocationProperty("sociedad");
		String codigo = (String) message.getInvocationProperty("codigo");
		String puntoventa = (String) message.getInvocationProperty("puntoventa");
		String letra = (String) message.getInvocationProperty("letra");
		String foliodesde = (String) message.getInvocationProperty("foliodesde");
		String foliohasta = (String) message.getInvocationProperty("foliohasta"); 
		String tipoRecepcion = (String) message.getInvocationProperty("tiporecepcion"); 
  
		HashMap<String,Object> documento = new HashMap<>(); 
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String dateToday = df.format(new Date());
		
		/* begin fernando */
		String systemSerialNumber = "";
		/* end fernando */
		
		// documento.put("odata.etag", purchaseOrder.get("odata.metadata"));
		
		documento.put("DocumentLines", new ArrayList<HashMap<String,Object>>());
		
		ArrayList<HashMap<String,Object>> values = (ArrayList<HashMap<String, Object>>) purchaseOrder.get("value");
		
		if(!values.isEmpty())
		{
			
			HashMap<String, Object> valor = values.get(0);
			
			Integer lineaMax = 0;
			
			// Get Max Line Number
			
			if(valor.containsKey("DocumentLines"))
			{
				 
				ArrayList<HashMap<String,Object>> lineas = (ArrayList<HashMap<String, Object>>) valor.get("DocumentLines");
				
				for(HashMap<String,Object> linea:lineas)
				{
					if(((Integer) linea.get("LineNum"))>lineaMax)
					{
						lineaMax = (Integer) linea.get("LineNum");
					}
				}
				
			}
			
			valor.remove("odata.etag");
			 
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
							
							Double qtyToSplit = 0.0;
							
							if(nuevaLinea.containsKey("RemainingOpenQuantity"))
							{
								Double qtyRemaningOpenQty = (Double) nuevaLinea.get("RemainingOpenQuantity");
								Double qty = (Double) articulosHM.get(linea.get("ItemCode"));
								if(qty>qtyRemaningOpenQty)
								{
									qtyToSplit = qty - qtyRemaningOpenQty;
									System.out.println("Qty pendiente a procesar " + qtyToSplit);
								}
							}
							
							
							nuevaLinea.replace("BaseLine", nuevaLinea.get("LineNum"));
							nuevaLinea.replace("BaseOpenQuantity", articulosHM.get(linea.get("ItemCode")));
							nuevaLinea.replace("DocEntry", valor.get("DocEntry")); 
							nuevaLinea.replace("ActualDeliveryDate", dateToday);
							if (tipoRecepcion.equals("oc")) 
								nuevaLinea.replace("BaseType", 22);
							else
								nuevaLinea.replace("BaseType", 18);
							nuevaLinea.replace("BaseEntry", valor.get("DocEntry"));
							
							// Cantidades Adicionales
							/* begin Fernando */
							if(qtyToSplit>0) {
								nuevaLinea.replace("Quantity", (Double) nuevaLinea.get("RemainingOpenQuantity")); 
								nuevaLinea.replace("PackageQuantity", nuevaLinea.get("RemainingOpenQuantity")); 
								nuevaLinea.replace("InventoryQuantity", nuevaLinea.get("RemainingOpenQuantity"));
							}
							else {
								nuevaLinea.replace("Quantity", articulosHM.get(linea.get("ItemCode")));
								nuevaLinea.replace("PackageQuantity", articulosHM.get(linea.get("ItemCode"))); 
								nuevaLinea.replace("InventoryQuantity", articulosHM.get(linea.get("ItemCode")));
							}
							
							if(nuevaLinea.containsKey("RemainingOpenInventoryQuantity"))
							{
								if(qtyToSplit>0) 
									nuevaLinea.replace("RemainingOpenInventoryQuantity", nuevaLinea.get("RemainingOpenQuantity")); 
								else
									nuevaLinea.replace("RemainingOpenInventoryQuantity", articulosHM.get(linea.get("ItemCode")));
							}
							/* end Fernando */
							
							
							// Batch Number
							
							HashMap<String,Object> batchNumber = new HashMap<>();
							
							batchNumber.put("BaseLineNumber", nuevaLinea.get("LineNum"));
							batchNumber.put("BatchNumber", valor.get("NumAtCard"));
							/* begin Fernando */
							if(qtyToSplit>0) 
								batchNumber.put("Quantity", nuevaLinea.get("RemainingOpenQuantity"));
							else
								batchNumber.put("Quantity", articulosHM.get(linea.get("ItemCode")));
							/* end Fernando */
							systemSerialNumber =  getSystemSerialNumber(message, valor.get("DocNum").toString(), tipoRecepcion);							
							batchNumber.put("SystemSerialNumber", systemSerialNumber);
							nuevaLinea.replace("BatchNumbers", new ArrayList<HashMap<String,Object>>());
							((ArrayList<HashMap<String,Object>>) nuevaLinea.get("BatchNumbers")).add(batchNumber);

							
							// DocumentLinesBinAllocations
							
							HashMap<String,Object> documentLinesBinAllocations = new HashMap<>();
							documentLinesBinAllocations.put("BaseLineNumber", nuevaLinea.get("LineNum"));
							documentLinesBinAllocations.put("SerialAndBatchNumbersBaseLine", 0);
							/* begin Fernando */
							if(qtyToSplit>0) 
								documentLinesBinAllocations.put("Quantity", nuevaLinea.get("RemainingOpenQuantity"));
							else 
								documentLinesBinAllocations.put("Quantity", articulosHM.get(linea.get("ItemCode")));
							/* end Fernando */
							String binAbsEntry = getBinAbsEntry(message, linea.get("WarehouseCode").toString());
							documentLinesBinAllocations.put("BinAbsEntry", binAbsEntry);
							
							nuevaLinea.put("DocumentLinesBinAllocations", new ArrayList<HashMap<String,Object>>());
							((ArrayList<HashMap<String,Object>>) nuevaLinea.get("DocumentLinesBinAllocations")).add(documentLinesBinAllocations);
							
							 
							((ArrayList<HashMap<String,Object>>) documento.get("DocumentLines")).add(nuevaLinea);
							
							nuevaLinea.remove("COGSAccountCode");
							
							// Genera la nueva linea spliteada
							
							if(qtyToSplit>0)
							{
								
								lineaMax = lineaMax +1;
								HashMap<String,Object> nuevaLineaSplit = new HashMap<>();
								nuevaLineaSplit.put("ItemCode", nuevaLinea.get("ItemCode"));
								nuevaLineaSplit.put("Price", nuevaLinea.get("Price"));
								nuevaLineaSplit.put("CostingCode", nuevaLinea.get("CostingCode"));
								nuevaLineaSplit.put("ProjectCode", nuevaLinea.get("ProjectCode"));
								nuevaLineaSplit.put("Quantity", qtyToSplit);
								nuevaLineaSplit.put("LineNum", lineaMax); 
								
								
								// DocumentLinesBinAllocations
								
								HashMap<String, Object> documentLinesBinAllocationsSplit = (HashMap<String, Object>) new HashMap<String, Object>();
								documentLinesBinAllocationsSplit.put("BaseLineNumber", lineaMax);
								documentLinesBinAllocationsSplit.put("SerialAndBatchNumbersBaseLine", 0);
								documentLinesBinAllocationsSplit.put("Quantity", qtyToSplit);
								documentLinesBinAllocationsSplit.put("BinAbsEntry", "1");
								
								ArrayList<HashMap<String, Object>> elementdocumentLinesBinAllocationsSplit = new ArrayList<HashMap<String, Object>>();
								elementdocumentLinesBinAllocationsSplit.add(documentLinesBinAllocationsSplit);
								
								nuevaLineaSplit.put("DocumentLinesBinAllocations", elementdocumentLinesBinAllocationsSplit);
								 
								// BatchNumbers
								
								HashMap<String, Object> batchNumbersSplit = (HashMap<String, Object>) new HashMap<String, Object>();
								batchNumbersSplit.put("BaseLineNumber", lineaMax);
								/* Begin Fernando */
								//batchNumbersSplit.put("SystemSerialNumber", 1);
								batchNumbersSplit.put("SystemSerialNumber", systemSerialNumber);
								/* End Fernando */
								batchNumbersSplit.put("Quantity", qtyToSplit);
								/* Begin Fernando */
								//batchNumbersSplit.put("BatchNumber", nuevaLinea.get("ProjectCode"));
								batchNumbersSplit.put("BatchNumber", valor.get("NumAtCard"));
								/* End Fernando */
								
								ArrayList<HashMap<String, Object>> elementBatchNumbersSplit = new ArrayList<HashMap<String, Object>>();
								elementBatchNumbersSplit.add(batchNumbersSplit);
								
								nuevaLineaSplit.put("BatchNumbers", elementBatchNumbersSplit);
								
								  
								((ArrayList<HashMap<String,Object>>) documento.get("DocumentLines")).add(nuevaLineaSplit);
							
							}
							 
							
							articulosHM.remove(linea.get("ItemCode")); // Remover el articulo procesado
							
						} 
						
					}
					
					// Procesar Lineas notificadas que no existen en la orden.
					
					for(Entry<String, Object> lineasPendientes: articulosHM.entrySet())
					{
						
						lineaMax = lineaMax +1;
						HashMap<String,Object> nuevaLinea = new HashMap<>();
				 
						nuevaLinea.put("ItemCode", lineasPendientes.getKey());
						//Double price = new Double(getBinAbsEntry(message, lineasPendientes.getKey().toString()));
						Double price = new Double(0.00);
						nuevaLinea.put("Price", price);
						nuevaLinea.put("CostingCode", "MA");
						nuevaLinea.put("ProjectCode", documento.get("Project"));
						nuevaLinea.put("Quantity", lineasPendientes.getValue());
						nuevaLinea.put("LineNum", lineaMax); 
						
						// DocumentLinesBinAllocations
						
						HashMap<String, Object> documentLinesBinAllocationsSplit = (HashMap<String, Object>) new HashMap<String, Object>();
						documentLinesBinAllocationsSplit.put("BaseLineNumber", lineaMax);
						documentLinesBinAllocationsSplit.put("SerialAndBatchNumbersBaseLine", 0);
						documentLinesBinAllocationsSplit.put("Quantity", lineasPendientes.getValue());
						documentLinesBinAllocationsSplit.put("BinAbsEntry", "1");
						
						ArrayList<HashMap<String, Object>> elementdocumentLinesBinAllocationsSplit = new ArrayList<HashMap<String, Object>>();
						elementdocumentLinesBinAllocationsSplit.add(documentLinesBinAllocationsSplit);
						
						nuevaLinea.put("DocumentLinesBinAllocations", elementdocumentLinesBinAllocationsSplit);
						 
						// BatchNumbers
						
						HashMap<String, Object> batchNumbersSplit = (HashMap<String, Object>) new HashMap<String, Object>();
						batchNumbersSplit.put("BaseLineNumber", lineaMax);
						/* Begin Fernando */
						if(systemSerialNumber.isEmpty()) {
							systemSerialNumber =  getSystemSerialNumber(message, valor.get("DocNum").toString(), tipoRecepcion);	
						}
						//batchNumbersSplit.put("SystemSerialNumber", 1);
						batchNumbersSplit.put("SystemSerialNumber", systemSerialNumber);
						/* End Fernando */
						batchNumbersSplit.put("Quantity", lineasPendientes.getValue());
						batchNumbersSplit.put("BatchNumber", documento.get("Project"));
						
						ArrayList<HashMap<String, Object>> elementBatchNumbersSplit = new ArrayList<HashMap<String, Object>>();
						elementBatchNumbersSplit.add(batchNumbersSplit);
						
						nuevaLinea.put("BatchNumbers", elementBatchNumbersSplit); 
						 
						((ArrayList<HashMap<String,Object>>) documento.get("DocumentLines")).add(nuevaLinea);
						
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
			if(tipoRecepcion.equals("importacion")) {
				documento.replace("ReserveInvoice","tNO");
			}
	
			
			
			((HashMap<String, Object>) documento.get("TaxExtension")).replace("NFRef", "Basado en Pedidos " + codigo);
			
			
		}
		  
		return documento;
	}
	
	private String getSystemSerialNumber(MuleMessage message, String docNum, String tipoRecepcion){
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
			String szTabla = "";
			if (tipoRecepcion.equals("oc")) 
				szTabla = "OPOR";
			else 
				szTabla = "OPCH";
			String Query = "SELECT T0.\"NumAtCard\" FROM "+sociedad+"." + szTabla + " T0 " + " WHERE T0.\"DocNum\" = '"+ docNum +"'";
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
	
	private String getPrice(MuleMessage message, String itemCode){
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
			String Query = "SELECT T2.\"LastPurPrc\" FROM "+sociedad+".OPCH T0 INNER JOIN " +sociedad+".PCH1 T1 ON T0.\"DocEntry\" = T1.\"DocEntry\" INNER JOIN OITM T2 ON T1.\"ItemCode\" = T2.\"ItemCode\" WHERE T0.\"ItemCode\" = '" + itemCode + "'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);
			HashMap<String, Object> queryResult = parseQueryPrice(querySet);
			LOG.info("Parsing done!");
			return (String) queryResult.get("lastPurPrc");
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				System.out.println("Fallo sql");
				return null;
			}
		}				
		return null;
	}	
	
	public HashMap<String, Object> parseQueryPrice(ResultSet set) throws SQLException {
		HashMap<String, Object> answer = new HashMap<>();
		while (set.next() != false) {
			answer.put("lastPurPrc", (set.getString("LastPurPrc")));
		}
		return answer;
	}		
}