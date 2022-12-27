package acqua.util.Transform;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class BuildConteoInventario extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh.log");
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		ArrayList<HashMap<String,Object>> articulos = (ArrayList<HashMap<String, Object>>) message.getInvocationProperty("consolidado");
		HashMap<String,Long> articulosHM = new HashMap<String,Long>();
		 
		for(HashMap<String,Object> articulo: articulos)
		{
			articulosHM.put((String) articulo.get("codigo"), (Long) articulo.get("cantidad")); 
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
								
								String keyLineaValue = val.getKey();
								Object lineaValue = val.getValue();
								
								nuevaLinea.put(keyLineaValue, lineaValue);
								
							}
							
							
							
							((ArrayList<HashMap<String,Object>>) documento.get("DocumentLines")).add(nuevaLinea); 
							
						} 
						
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
			documento.replace("Letter",puntoventa);
			documento.replace("FolioNumberFrom",foliodesde);
			documento.replace("FolioNumberTo",foliohasta);
			documento.replace("DocDate",dateToday);
			documento.replace("DocDueDate",dateToday);
			documento.replace("TaxDate",dateToday);
			documento.replace("CreationDate",dateToday);
			documento.replace("UpdateDate",dateToday);
			documento.replace("Series","17");
			documento.replace("FinancialPeriod","11");
			documento.replace("WareHouseUpdateType","dwh_OrdersFromVendors");
			documento.replace("U_NroCompEsp","");
			
			((HashMap<String, Object>) documento.get("TaxExtension")).replace("NFRef", "Basado en Pedidos " + codigo);
			
			
		}
		  
		return documento;
	}
	
	
	
}
