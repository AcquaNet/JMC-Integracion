package b1_sync_stock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class WorkUtils {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	public static JSONObject StringToJson(String str) {
		return new JSONObject(str);
	}

	// Merge entre list y returnList - Agrega todos los
	// objetos de list a returnList y saltea los que ya existen en returnList
	public static List<HashMap<String, Object>> addOntoList(String identifier, List<HashMap<String, Object>> list,
			List<HashMap<String, Object>> list2) {
		LOG.info("JAVA.WorkUtils.25: addOntoList Called");
		SortedSet<HashMap<String, Object>> set = new TreeSet<HashMap<String, Object>>(new CodeComparator(identifier));
		if (list2 != null) {
			LOG.info("JAVA.WorkUtils.28: Add List from Argument 3 to SortedSet");
			set.addAll(list2);
		}
		if (list != null) {
			LOG.info("JAVA.WorkUtils.32: Add List from Argument 2 to SortedSet");
			set.addAll(list);
		}
		List<HashMap<String, Object>> returnList = new ArrayList<>();
		LOG.info("JAVA.WorkUtils.36: Add all objects from SortedSet to a list and return the list");
		returnList.addAll(set);
		return returnList;
	}
	
	//Combine lists
	public static List<HashMap<String, Object>> combineLists(List<HashMap<String, Object>> list,
			List<HashMap<String, Object>> list2) {
		list2.addAll(list);
		return list2;
	}

	// Creo una lista vacia
	public static List<HashMap<String, Object>> createList() {
		LOG.info("JAVA.WorkUtils.43: createList called");
		List<HashMap<String, Object>> returnList = new ArrayList<HashMap<String, Object>>();
		return returnList;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> removeStandardPrice(HashMap<String, Object> py) {
		LOG.info("JAVA.WorkUtils.50: removeStandardPrice called");
		if (py.get("ItemWarehouseInfoCollection") != null) {
			for (HashMap<String, Object> map : (List<HashMap<String, Object>>) py.get("ItemWarehouseInfoCollection")) {
				map.remove("StandardAveragePrice");
			}
		}
		return py;
	}

	public static HashMap<String, Object> convertInsideJSON(HashMap<String, Object> map) {
		for (String str : map.keySet()) {
			LOG.info(map.get(str));
			JSONObject obj = new JSONObject((String) map.get(str));

			String[] destinations = obj.getString("destinations").split(",");
			HashMap<String, Boolean> trueMap = new HashMap<String, Boolean>();
			for (String key : destinations) {
				trueMap.put(key, true);
			}
			map.put(str, trueMap);
		}
		return map;
	}

	@SuppressWarnings({ "unchecked" })
	public static ArrayList<HashMap<String, Object>> SplitMultipleDestinations(String which,
			ArrayList<HashMap<String, Object>> payload, HashMap<String, String> destinationMap) {
		LOG.info("JAVA.WorkUtils.77: Orders Splitting initiated");
		if (!which.equals("Orders"))
		{
			LOG.info("JAVA.WorkUtils.77: Orders Splitting initiated");
			return payload;
		}
		else {
			// Lista en blanco
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			// Loopeo por cada orden en el payload
			for (HashMap<String, Object> map : payload) {
				// Lineas del documento
				ArrayList<HashMap<String, Object>> lines = (ArrayList<HashMap<String, Object>>) map
						.get("DocumentLines");
				// Saco las lineas despues de guardarlas al arraylist para dejar un documento limpio
				LOG.info("JAVA.WorkUtils.89: DocNum of this document being processed: "+map.get("DocNum"));
				LOG.info("JAVA.WorkUtils.90: Ammount of lines:" + lines.size());
				map.remove("DocumentLines");
				// Remuevo datos de valor total para que lo re-calcule SAP
				map.remove("DocTotal");
				map.remove("VatSum");
				map.remove("VatSumSys");
				// Creo un mapa para guardar ArrayList de lineas de cada destino
				HashMap<String, Object> possibleMaps = new HashMap<String, Object>();
				// Loopeo por todas las lineas
				for (HashMap<String, Object> lineMap : lines) {
					// Obtengo el nobmre del destino correcto en base al codigo de almacen
					// System.out.println("LineMap destination WH: "+lineMap.get("WarehouseCode"));
					String destinationAssigned = destinationMap.get((String) lineMap.get("WarehouseCode"));
					// System.out.println("DestinationMap values");
					// Todas las lineas van al almacen principal, 01
					lineMap.put("WarehouseCode", "01");
					// Reviso si ya hay lineas para el destino
					if (possibleMaps.containsKey(destinationAssigned)
							&& possibleMaps.get(destinationAssigned) != null) {
						// System.out.println("Assigned destination: "+destinationAssigned);
						// Si las hay, obtengo el array
						ArrayList<HashMap<String, Object>> ListOfLines = (ArrayList<HashMap<String, Object>>) possibleMaps
								.get(destinationAssigned);
						// Agrego la linea a ese array
						ListOfLines.add(lineMap);
						// Guardo la lista al hashmap
						possibleMaps.put(destinationAssigned, ListOfLines);
					}
					// Si todavia no hay lineas para el destino;
					else {
						// Creo el array list
						ArrayList<HashMap<String, Object>> ListOfLines = new ArrayList<HashMap<String, Object>>();
						// Agrego la linea a ese array
						ListOfLines.add(lineMap);
						// Guardo la lista al hashmap
						possibleMaps.put(destinationAssigned, ListOfLines);
					}
				}
				LOG.info("JAVA.WorkUtils.128: Ammount of destinations: " + possibleMaps.keySet().size());
				// Loopeo por cada lista de lineas (segun destinacion)
				for (String destination : possibleMaps.keySet()) {
					// Obtengo la lista
					ArrayList<HashMap<String, Object>> documentLines = (ArrayList<HashMap<String, Object>>) possibleMaps
							.get(destination);
					// Hago un clon del documento vacio sin documentLines
					HashMap<String, Object> mapClone = (HashMap<String, Object>) map.clone();
					// Le guardo las lineas correspondientes
					mapClone.put("DocumentLines", documentLines);
					// Guardo el destino de esta orden para filtrar despues en el forEach
					mapClone.put("destination", destination);
					// System.out.println("Destination set: "+destination);
					// Guardo la orden de venta a la nueva lista
					list.add(mapClone);
					LOG.info("JAVA.WorkUtils.143: Added an order for destination " + destination);
				}
			}
			return list;
		}
	}

	public static HashMap<String, Object> removeWarehouseInfo(String which, HashMap<String, Object> item) {
		if (which.equals("Items"))
			item.remove("ItemWarehouseInfoCollection");
		return item;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> removeUsedBarcodes(String which, HashMap<String, Object> existing,
			HashMap<String, Object> newMap) {
		if (which.equals("Items")) {
			LOG.info("JAVA.WorkUtils.160: Item is now being processed");
			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) newMap
					.get("ItemBarCodeCollection");
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			int i = 0;
			for (HashMap<String, Object> map : list) {
				LOG.info("JAVA.WorkUtils.165:Existing barcode: " + map.get("Barcode"));
				Boolean exists = false;
				if ((existing.get("ItemBarCodeCollection") != null)
						? existing.get("ItemBarCodeCollection").getClass().equals(ArrayList.class)
						: false) {
					for (HashMap<String, Object> destMap : (ArrayList<HashMap<String, Object>>) existing
							.get("ItemBarCodeCollection")) {
						LOG.info("JAVA.WorkUtils.169:Barcode to check equals: " + destMap.get("Barcode"));
						if (map.get("Barcode").equals(destMap.get("Barcode"))) {
							exists = true;
							LOG.info("JAVA.WorkUtils.172:Barcode exists!");
						}
					}
					if (exists) {
						toRemove.add(i);
					}
				}
				i++;
			}
			for (int a : toRemove) {
				list.remove(a);
			}
			toRemove.clear();
			newMap.put("ItemBarCodeCollection", list);
		}
		return newMap;
	}

	public static ArrayList<HashMap<String, Object>> CheckIfOk(ArrayList<HashMap<String, Object>> payload) {
		int i = 0;
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		for (HashMap<String, Object> map : payload) {
			String str = StringToJSON.javaToJSONToString(map);
			if (str.contains("No matching records found")) {
				toRemove.add(i);
			}
			i++;
		}
		for (int a : toRemove) {
			payload.remove(a);
		}
		return payload;
	}
	
	public static HashMap<String, Object> setDestinationFromWh(String which, HashMap<String, Object> item) {
		if (which.equals("Orders")) {

		}
		return item;
	}
}

class CodeComparator implements Comparator<HashMap<String, Object>> {
	String identifier;

	public CodeComparator(String identifier) {
		super();
		this.identifier = identifier;
	}

	@Override
	public int compare(HashMap<String, Object> obj1, HashMap<String, Object> obj2) {

		if (obj1.containsKey(identifier) && (obj2.containsKey(identifier)))
			return ((String) obj1.get(identifier)).compareTo(((String) obj2.get(identifier)));
		else
			return 0;
	}
}
