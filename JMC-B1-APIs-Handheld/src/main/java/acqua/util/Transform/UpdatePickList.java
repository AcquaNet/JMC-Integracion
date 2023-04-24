package acqua.util.Transform;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;


import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class UpdatePickList extends AbstractMessageTransformer implements MuleContextAware {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		/* Se recupera informacion */
		ArrayList<HashMap<String, Object>> inputArticulos = message.getInvocationProperty("listaDeArticulos");
		HashMap<String, Object> pickList = (HashMap<String, Object>) message.getPayload();
		HashMap<Integer, Integer> BinAbsList = (HashMap<Integer, Integer>) message.getInvocationProperty("PickListAbs");
		Integer cantBultos = (Integer) message.getInvocationProperty("cantBulto");
		String sociedad = (String) message.getInvocationProperty("sociedad");
		String entorno = (String) message.getInvocationProperty("entorno");
		HashMap<String, Object> batchNumbers = new HashMap<String, Object>();

		/* begin 03/23 */
		ArrayList<HashMap<String, Object>> inputArticulosUnificado = consolidarArticulos(inputArticulos);
		/* end 03/23 */
		
		boolean buscarBatch = true;
		HashMap<String, Object> pickListCopy = new HashMap<>();
		ArrayList<HashMap<String, Object>> pickLines = (ArrayList<HashMap<String, Object>>) pickList
				.get("PickListsLines");
		ArrayList<HashMap<String, Object>> pickLinesCopy = new ArrayList<>();
		/* begin 03/23 */
		//for (HashMap<String, Object> itemLine : inputArticulos) {
		/* end 03/23 */
		for (HashMap<String, Object> itemLine : inputArticulosUnificado) {
			Integer newCount;
			for (HashMap<String, Object> originalLine : pickLines) {
				if (itemLine.get("linenum").equals(originalLine.get("LineNumber"))) {
					if (!originalLine.get("PickStatus").equals("ps_Closed")) {
						double cantidadRecibida = (double) itemLine.get("cantidad");
						newCount = (int) cantidadRecibida;
						Integer LineQtty = ((int) ((double) originalLine.get("PickedQuantity"))) + newCount;
						originalLine.put("PickedQuantity", LineQtty);
						
						Integer baseObjectType = (Integer) originalLine.get("BaseObjectType");
						if(baseObjectType == 1250000001) {
							if(buscarBatch) {
								String orderEntry = originalLine.get("OrderEntry").toString();
								batchNumbers = postInventoryTransfer(orderEntry, sociedad, entorno);
								buscarBatch = false;
							}
						}
						
						/* begin 03/23 */
						/*
						String codigoItem = (String) itemLine.get("codigo");
						HashMap<String, Object> lotes =  getLotesPickingByItem(message, codigoItem);
						ArrayList<HashMap<String, Object>> listCopy = new ArrayList<>();
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("BaseLineNumber", originalLine.get("LineNumber"));
						map.put("Quantity", LineQtty);
						if(baseObjectType == 1250000001) {
							//String batchNumber = getBatchNumber(batchNumbers, (Integer) originalLine.get("LineNumber"));
							String batchNumber = getBatchNumber(batchNumbers, (Integer) originalLine.get("OrderRowID"));
							map.put("BatchNumber", batchNumber);
							//String sysNumber = getSysNumber(batchNumbers, (Integer) originalLine.get("LineNumber"));
							String sysNumber = getSysNumber(batchNumbers, (Integer) originalLine.get("OrderRowID"));
							map.put("SystemSerialNumber", sysNumber);							
						}
						else {
							map.put("BatchNumber", (String) lotes.get("distNumber"));
							map.put("SystemSerialNumber", (String) lotes.get("sysNumber"));
						}
						listCopy.add(map);
						*/
						
						ArrayList<HashMap<String, Object>> listCopy = new ArrayList<>();
						for (HashMap<String, Object> itemLineUnificado : (ArrayList<HashMap<String, Object>>) itemLine.get("batches")) {
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("BaseLineNumber", originalLine.get("LineNumber"));
							map.put("SystemSerialNumber", itemLineUnificado.get("sysnumber"));	
							map.put("BatchNumber", itemLineUnificado.get("distnumber"));
							map.put("Quantity", itemLineUnificado.get("cantidad"));
							listCopy.add(map);
						}
						
						/* end 03/23 */
									
						// Update also allocations, only will insert into first
						/* begin - 03/23 */
						/*
						ArrayList<HashMap<String, Object>> AllocationList = (ArrayList<HashMap<String, Object>>) originalLine
								.get("DocumentLinesBinAllocations");
						ArrayList<HashMap<String, Object>> AllocationListCopy = new ArrayList<>();
						HashMap<String, Object> backupAbs = new HashMap<>();
						//backupAbs.put("BinAbsEntry", "1");
						String warehouse = getFromWarehouse(batchNumbers);
						if(baseObjectType == 1250000001) {
							String absEntry =  getBinAbsEntry(message, warehouse);
							backupAbs.put("BinAbsEntry", absEntry);							
						}
						else {
							backupAbs.put("BinAbsEntry", "1");							
						}

						backupAbs.put("Quantity", LineQtty);
						backupAbs.put("AllowNegativeQuantity", "tNO");
						backupAbs.put("BaseLineNumber", originalLine.get("LineNumber"));

						backupAbs.put("SerialAndBatchNumbersBaseLine", 0);

						AllocationListCopy.add(backupAbs);					
						*/
						
						ArrayList<HashMap<String, Object>> AllocationListCopy = new ArrayList<>();
						for (HashMap<String, Object> itemLineUnificado : (ArrayList<HashMap<String, Object>>) itemLine.get("batches")) {
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("BaseLineNumber", originalLine.get("LineNumber"));
							map.put("SerialAndBatchNumbersBaseLine", 0);	
							map.put("Quantity", itemLineUnificado.get("cantidad"));
							map.put("AllowNegativeQuantity", "tNO");
							
							/* begin - 04/23 */
							Integer orderEntry = (Integer) originalLine.get("OrderEntry");
							Integer orderRowId = (Integer) originalLine.get("OrderRowID");
							
							String pcodigoitem = (String) itemLine.get("codigo");
							String pbatchnumber = (String) itemLineUnificado.get("distnumber");							
							/* end - 04/23 */							
							
							String absEntry = "";
							if(baseObjectType == 1250000001) {
								String warehouse = getFromWarehouse(batchNumbers);
								absEntry =  getBinAbsEntry(message, pcodigoitem, pbatchnumber, warehouse);
							}
							else {
								/* begin 04/23 */
								//map.put("BinAbsEntry", "1");
								String pwarehouse = getWarehouse(message, orderEntry, orderRowId);
								absEntry =  getBinAbsEntry(message, pcodigoitem, pbatchnumber, pwarehouse);
								/* end 04/23 */
							}
							map.put("BinAbsEntry", absEntry);
							/* end 04/23 */			
							
							AllocationListCopy.add(map);
						}						
						
						/* end - 03/23 */



						originalLine.put("BatchNumbers", listCopy);
						originalLine.put("DocumentLinesBinAllocations", AllocationListCopy);
						
						originalLine.remove("ReleasedQuantity");
						originalLine.remove("PreviouslyReleasedQuantity");
						/* Begin Fernando  100223 */
						originalLine.remove("PickStatus");
						/* End Fernando  100223 */
					}
					pickLinesCopy.add(originalLine);
				}

			}
		}
		// Nivel 0 - Lista
		pickListCopy.put("PickListsLines", pickLinesCopy);
		
		// Nivel 0 - Header
		/* Begin Fernando  100223 */
		//pickListCopy.put("Status", pickList.get("Status"));
		/* End Fernando  100223 */
		pickListCopy.put("ObjectType", pickList.get("ObjectType"));
		pickListCopy.put("Absoluteentry", pickList.get("Absoluteentry"));
		pickListCopy.put("PickDate", pickList.get("PickDate"));
		pickListCopy.put("UseBaseUnits", pickList.get("UseBaseUnits"));
		pickListCopy.put("U_CantBultos", cantBultos);

		return pickListCopy;
	}

	/*
	 * getLotesPickingByItem
	 */
	/* begin 03/23 */
	/*
	private HashMap<String, Object> getLotesPickingByItem(MuleMessage message, String codigoItem){
		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		
		String sociedad = message.getInvocationProperty("sociedad");

		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Object connect = manager.connect();
		// Create a response layout
		
		if (sociedad == null)
		{
			System.out.println("Sociedad no mapeada");
			return null;
		}
		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			System.out.println("Fallo conexion a BD");
			return null;
		}
		System.out.println("Connection to HANA successful!");
		try {
			// Create a statement to call
			manager.createStatement();

			// Query
			String Query = "SELECT TOP 1 T0.\"ItemCode\",T0.\"SysNumber\",T0.\"DistNumber\" FROM "+sociedad+".OBTN T0 " + " WHERE T0.\"ItemCode\" = '"+codigoItem+"'";
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			HashMap<String, Object> queryResult = parseQueryLotes(querySet);
			LOG.info("Parsing done!");

			return queryResult;
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				System.out.println("Fallo sql");
				return null;
			}
		}		
		
		return null;
	}
	*/
	/* end 03/23 */

	/*
	 * getBinAbsEntry
	 */
	/* begin 04/23 */
	//private String getBinAbsEntry(MuleMessage message, String wareHouse){
	private String getBinAbsEntry(MuleMessage message, String itemCode, String batchNumber, String warehouse){	
    /* end 04/23 */
		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");
		
		String sociedad = message.getInvocationProperty("sociedad");

		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);
		// Connect to DB
		Object connect = manager.connect();
		// Create a response layout
		if (sociedad == null)
		{
			System.out.println("Sociedad no mapeada");
			return null;
		}
		if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
			System.out.println("Fallo conexion a BD");
			return null;
		}
		System.out.println("Connection to HANA successful!");
		try {
			// Create a statement to call
			manager.createStatement();

			// Query
			/* begin 04/23 */
			// String Query = "SELECT T0.\"AbsEntry\" FROM "+sociedad+".OBIN T0 WHERE T0.\"WhsCode\" = '"+wareHouse+"' AND T0.\"SysBin\" = 'Y'";
			// String Query = "SELECT T0.\"AbsEntry\" FROM "+sociedad+".OBTN T0 WHERE T0.\"ItemCode\" = '"+itemCode+"' AND T0.\"DistNumber\" = '"+ batchNumber + "'";
			String Query = "SELECT T1.\"BinAbs\" FROM "+sociedad+".OBTN T0 JOIN " + sociedad +".OBBQ T1 ON T0.\"AbsEntry\" = T1.\"SnBMDAbs\" AND T0.\"ItemCode\" = T1.\"ItemCode\" WHERE T0.\"ItemCode\" = '"+itemCode+"' AND T0.\"DistNumber\" = '"+ batchNumber + "' AND T1.\"WhsCode\" = '" + warehouse + "'";
			
			/* end 04/23 */
			System.out.println("Query: " + Query);
			ResultSet querySet = manager.executeQuery(Query);

			HashMap<String, Object> queryResult = parseQueryBinAbsEntry(querySet);
			LOG.info("Parsing done!");
			return (String) queryResult.get("AbsEntry");
			//return queryResult.get(");
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				System.out.println("Fallo sql");
				return null;
			}
		}		
		
		return null;
	}	
	
	/*
	 * parseQueryLotes
	 */
	/* begin - 03/23 */
	/*
	public HashMap<String, Object> parseQueryLotes(ResultSet set) throws SQLException {

		HashMap<String, Object> answer = new HashMap<>();

		while (set.next() != false) {
			answer.put("itemCode", (set.getString("ItemCode")));
			answer.put("sysNumber", (set.getString("SysNumber")));
			answer.put("distNumber", (set.getString("DistNumber")));
		}
		return answer;
	}
	*/
	/* end - 03/23 */
	
	/*
	 * parseQueryBinAbsEntry
	 */
	public HashMap<String, Object> parseQueryBinAbsEntry(ResultSet set) throws SQLException {

		HashMap<String, Object> answer = new HashMap<>();

		while (set.next() != false) {
			/* begin - 04/23 */
			//answer.put("AbsEntry", (set.getString("AbsEntry")));
			answer.put("AbsEntry", (set.getString("BinAbs")));
			/* end - 04/23 */
		}
		return answer;
	}	
	
	/*
	 * getBatchNumber
	 */
	/* begin - 03/23 */
	/*
	private String getBatchNumber(HashMap<String, Object> batchLines, Integer LineNumber) {
		
		ArrayList map = (ArrayList) batchLines.get("StockTransferLines");
		
		for (int i=0;i<map.size();i++) {
		      
		      System.out.println(map.get(i));
		      HashMap obj = (HashMap) map.get(i);
		      if(obj.get("LineNum").equals(LineNumber)) {
		    	  ArrayList map2 = (ArrayList) obj.get("BatchNumbers");
		    	  HashMap obj2 = (HashMap) map2.get(0);
		    	  return (String) obj2.get("BatchNumber");
		      }
		    }
		
		return "";
	}
	*/
	/* end - 03/23 */
	

	/*
	 * getSysNumber
	 */
	/* begin - 03/23 */
	/*
	private String getSysNumber(HashMap<String, Object> batchLines, Integer LineNumber) {
		
		ArrayList map = (ArrayList) batchLines.get("StockTransferLines");
		
		for (int i=0;i<map.size();i++) {
		      
		      System.out.println(map.get(i));
		      HashMap obj = (HashMap) map.get(i);
		      if(obj.get("LineNum").equals(LineNumber)) {
		    	  ArrayList map2 = (ArrayList) obj.get("BatchNumbers");
		    	  HashMap obj2 = (HashMap) map2.get(0);
		    	  Integer sysNumber = (Integer) obj2.get("SystemSerialNumber");
		    	  return sysNumber.toString();
		      }
		    }
		
		return "";
	}
	*/
	/* end - 03/23 */

	/*
	 * getFromWarehouse
	 */
	private String getFromWarehouse(HashMap<String, Object> batchLines) {		
		String from = (String) batchLines.get("FromWarehouse");
		return from;
	}	
	
	/*
	 * Service Layer: InventoryTransferRequests
	 */
	private static HashMap<String, Object> postInventoryTransfer(String orderEntry, String sociedad, String entorno) {
        try {
            URL url = new URL("http://localhost:8081/b1/hh/v1/InventoryTransferRequests/" + orderEntry);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
        	String jsonInputString = "{\"entorno\": \"" + entorno + "\", \"sociedad\": \"" + sociedad + "\"}";        	
        	con.setDoOutput(true);
        	try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);			
            }           

            int status = con.getResponseCode();
            if(status == 200){
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                    System.out.println(inputLine);
                }
                in.close();
                con.disconnect();
            	
				// Convert JSON to Map and return data
				HashMap<String, Object> inputMap = (HashMap<String, Object>) acqua.util.JSONUtil
						.stringToMap(content.toString());
				if (inputMap == null) {
					return null;
				}
				else return inputMap;
            	
            }

        	return null;


        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
		
	}
	
	/* begin 03/23 */
	/* nueva funcion: consolidarArticulos */
	private ArrayList<HashMap<String, Object>> consolidarArticulos(ArrayList<HashMap<String, Object>> listaOriginal) {
		ArrayList<HashMap<String, Object>> listaConsolidada = new ArrayList<>();
		for (HashMap<String, Object> itemLine : listaOriginal) {
			boolean existe = false;
			for (HashMap<String, Object> itemLineConsolidado : listaConsolidada) {
				if (itemLine.get("linenum").equals(itemLineConsolidado.get("linenum"))) {
					double cantidadConsolidada = (double) itemLine.get("cantidad") + (double) itemLineConsolidado.get("cantidad");
					itemLineConsolidado.remove("cantidad");
					itemLineConsolidado.put("cantidad", cantidadConsolidada);
					
					HashMap<String, Object> batchNew = new HashMap<String, Object>();
					batchNew.put("cantidad", itemLine.get("cantidad"));
					batchNew.put("distnumber", itemLine.get("distnumber"));
					batchNew.put("sysnumber", itemLine.get("sysnumber"));
					
					ArrayList<HashMap<String, Object>> listaBatches = (ArrayList<HashMap<String, Object>>) itemLineConsolidado.get("batches");
					listaBatches.add(batchNew);					
					itemLineConsolidado.remove("batches");
					itemLineConsolidado.put("batches", listaBatches);
					
					existe = true;
				}
			}
			if(!existe) {
				HashMap<String, Object> itemLineNew = new HashMap<String, Object>();
				itemLineNew.put("linenum", itemLine.get("linenum"));
				itemLineNew.put("codigo", itemLine.get("codigo"));
				itemLineNew.put("cantidad", itemLine.get("cantidad"));
				ArrayList<HashMap<String, Object>> batches = new ArrayList<>();
				HashMap<String, Object> batchNew = new HashMap<String, Object>();
				batchNew.put("cantidad", itemLine.get("cantidad"));
				batchNew.put("distnumber", itemLine.get("distnumber"));
				batchNew.put("sysnumber", itemLine.get("sysnumber"));
				batches.add(batchNew);
				itemLineNew.put("batches", batches);
				
				listaConsolidada.add(itemLineNew);
			}
		}
		return listaConsolidada;
	}
	/* end 03/23 */
	
	
	/* begin 04/23 */
	private String getWarehouse(MuleMessage message, Integer docEntry, Integer linNum){	

			String user = message.getInvocationProperty("DBUser");
			String password = message.getInvocationProperty("DBPass");
			String connectionString = message.getInvocationProperty("DBConnection");
			
			String sociedad = message.getInvocationProperty("sociedad");

			ODBCManager manager = new ODBCManager(user, password, connectionString);
			Object connect = manager.connect();
			if (sociedad == null)
			{
				System.out.println("Sociedad no mapeada");
				return null;
			}
			if (!connect.getClass().equals(Connection.class) && !connect.getClass().equals(com.sap.db.jdbc.HanaConnectionFinalize.class)) {
				System.out.println("Fallo conexion a BD");
				return null;
			}
			System.out.println("Connection to HANA successful!");
			try {
				manager.createStatement();

				String Query = "SELECT T0.\"WhsCode\" FROM "+sociedad+".RDR1 T0 WHERE T0.\"DocEntry\" = " + docEntry + " AND T0.\"LineNum\" = "+ linNum;
				
				System.out.println("Query: " + Query);
				ResultSet querySet = manager.executeQuery(Query);

				HashMap<String, Object> queryResult = parseQueryWarehouse(querySet);
				LOG.info("Parsing done!");
				return (String) queryResult.get("WhsCode");
			} catch (Exception e) {
				e.printStackTrace();
				if (e.getClass().getName().contains("SQLException")) {
					System.out.println("Fallo sql");
					return null;
				}
			}		
			
			return null;
		}	
	
	
	/*
	 * parseQueryWarehouse
	 */
	public HashMap<String, Object> parseQueryWarehouse(ResultSet set) throws SQLException {

		HashMap<String, Object> answer = new HashMap<>();

		while (set.next() != false) {
			answer.put("WhsCode", (set.getString("WhsCode")));
		}
		return answer;
	}		
	/* end - 04/23 */
}
