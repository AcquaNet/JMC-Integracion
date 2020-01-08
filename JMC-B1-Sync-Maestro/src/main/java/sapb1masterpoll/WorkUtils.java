package sapb1masterpoll;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.mule.DefaultMuleEvent;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class WorkUtils {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	private static final Logger FatalLog = Logger.getLogger("jmc_fatal.log");

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

	// Combine lists
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
		if (py.get("DocumentLines") != null) {
			ArrayList<HashMap<String, Object>> lines = (ArrayList<HashMap<String, Object>>) py.get("DocumentLines");
			ArrayList<HashMap<String, Object>> linesDupe = new ArrayList<>();
			if (lines != null) {
				for (HashMap<String, Object> map : lines) {
					if (map != null) {
						if (map.containsKey("COGSAccountCode")) {
							map.remove("COGSAccountCode");
						}
						linesDupe.add(map);
					}
					py.put("DocumentLines", linesDupe);
				}
			}
		}
		if (py.containsKey("AttachmentEntry")) {
			py.remove("AttachmentEntry");
		}
		if (py.containsKey("ItemUnitOfMeasurementCollection")) {
			py.remove("ItemUnitOfMeasurementCollection");
		}
		if (py.containsKey("DocNum")) {
			py.remove("DocNum");
		}
		if (py.containsKey("DocEntry")) {
			py.remove("DocEntry");
		}
		// if (py.containsKey("BarCode")) {
		// py.remove("BarCode");
		// }
		// if (py.containsKey("BaseUnitName")) {
		// py.remove("BaseUnitName");
		// }
		// if (py.containsKey("BeverageGroupCode")) {
		// py.remove("BeverageGroupCode");
		// }
		// if (py.containsKey("BeverageTableCode")) {
		// py.remove("BeverageTableCode");
		// }
		// if (py.containsKey("CapitalGoodsOnHoldLimit")) {
		// py.remove("CapitalGoodsOnHoldLimit");
		// }
		// if (py.containsKey("CapitalGoodsOnHoldPercent")) {
		// py.remove("CapitalGoodsOnHoldPercent");
		// }
		// if (py.containsKey("CapitalizationDate")) {
		// py.remove("CapitalizationDate");
		// }
		// if (py.containsKey("DataExportCode")) {
		// py.remove("DataExportCode");
		// }
		// if (py.containsKey("DefaultCountingUnit")) {
		// py.remove("DefaultCountingUnit");
		// }
		// if (py.containsKey("DefaultCountingUoMEntry")) {
		// py.remove("DefaultCountingUoMEntry");
		// }
		// if (py.containsKey("DefaultPurchasingUoMEntry")) {
		// py.remove("DefaultPurchasingUoMEntry");
		// }
		// if (py.containsKey("DefaultSalesUoMEntry")) {
		// py.remove("DefaultSalesUoMEntry");
		// }
		// if (py.containsKey("DefaultWarehouse")) {
		// py.remove("DefaultWarehouse");
		// }
		// if (py.containsKey("DefaultCountingUoMEntry")) {
		// py.remove("DefaultCountingUoMEntry");
		// }
		// if (py.containsKey("DefaultCountingUoMEntry")) {
		// py.remove("DefaultCountingUoMEntry");
		// }
		// if (py.containsKey("DefaultCountingUoMEntry")) {
		// py.remove("DefaultCountingUoMEntry");
		// }

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

	public static String PostIfItsACancelledPurchaseOrder(HashMap<String, Object> payload, String restType,
			String Path) {
		// Switch to POST if its a PurchaseOrder cancellation
		if (Path.equals("PurchaseOrders")) {
			if (payload.containsKey("Cancelled")) {
				if (payload.get("Cancelled").equals("tYES")) {
					return "POST";
				}
			}
		}
		return "PATCH";
	}

	public static String CheckIfItsACancelledPurchaseOrder(HashMap<String, Object> payload, String originalURL,
			Object documentEntry, Boolean Patch, String type, String actualOrigUrl) {
		if (Patch) {
			if (originalURL.equals("PurchaseOrders")) {
				if (payload.containsKey("Cancelled")) {
					if (payload.get("Cancelled").equals("tYES")) {
						// We know its an integer anyway
						return originalURL + "(" + documentEntry.toString() + ")/Cancel";
					}
				}
			}

			return actualOrigUrl;

		} else {
			// POST Input
			// originalURL = PurchaseOrders
			if (originalURL.equals("PurchaseOrders")) {
				if (payload.containsKey("Cancelled")) {
					if (payload.get("Cancelled").equals("tYES")) {
						return originalURL + "(" + documentEntry.toString() + ")/Cancel";
					}
				}
			}
			return originalURL;
		}
	}

	@SuppressWarnings({ "unchecked" })
	public static ArrayList<HashMap<String, Object>> SplitMultipleDestinations(String which,
			ArrayList<HashMap<String, Object>> payload, HashMap<String, String> destinationMap, String user,
			String password, String connectionString, String destinations) {
		LOG.info("JAVA.WorkUtils.156: Orders Splitting initiated");
		if (payload.size() == 0) {
			return payload;
		}
		if (which.equals("Orders")) {			
			return payload;
		} else if (which.equals("PurchaseOrders")) {
			// Lista en blanco
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			// Loopeo por cada orden en el payload
			for (HashMap<String, Object> map : payload) {
				// Lineas del documento
				ArrayList<HashMap<String, Object>> lines = (ArrayList<HashMap<String, Object>>) map
						.get("DocumentLines");
				// Saco las lineas despues de guardarlas al arraylist para dejar un documento
				// limpio
				LOG.info("JAVA.WorkUtils.275: DocNum of this PurchaseOrder document being processed: "
						+ map.get("DocNum"));

				// Creo un mapa para guardar ArrayList de lineas de cada destino
				HashMap<String, Object> possibleMaps = new HashMap<String, Object>();
				// Loopeo por todas las lineas
				if (lines == null) {

					FatalLog.info("JAVA.WorkUtils.282: Document lines are null?? ERROR");
					FatalLog.info("JAVA.WorkUtils.283: Document with issues: " + JSONUtil.javaToJSONToString(map));
					// return null;
				} else {
					if (map.get("U_Pd_Soc") != null ? !map.get("U_Pd_Soc").equals("") : false) {
						String destinationAssigned = destinationMap.get(map.get("U_Pd_Soc"));
						// LOG.info("JAVA.WorkUtils.128: Ammount of destinations: " +
						// possibleMaps.keySet().size());
						// Loopeo por cada lista de lineas (segun destinacion)
						if (map.containsKey("DocTotal")) {
							map.remove("DocTotal");
						}
						if (map.containsKey("VatSum")) {
							map.remove("VatSum");
						}
						if (map.containsKey("VatSumSys")) {
							map.remove("VatSumSys");
						}
						if (map.containsKey("VatSumFC")) {
							map.remove("VatSumFC");
						}
						ArrayList<HashMap<String, Object>> newLines = new ArrayList<>();
						for (HashMap<String, Object> line : lines) {
							if (line.containsKey("NetTaxAmount")) {
								line.remove("NetTaxAmount");
							}
							if (line.containsKey("NetTaxAmountFC")) {
								line.remove("NetTaxAmountFC");
							}
							if (line.containsKey("NetTaxAmountSC")) {
								line.remove("NetTaxAmountSC");
							}
							if (line.containsKey("PriceAfterVAT")) {
								line.remove("PriceAfterVAT");
							}
							if (line.containsKey("LineTotal")) {
								line.remove("LineTotal");
							}
							if (line.containsKey("TaxPercentagePerRow")) {
								line.remove("TaxPercentagePerRow");
							}
							if (line.containsKey("TaxTotal")) {
								line.remove("TaxTotal");
							}
							if (line.containsKey("TaxPerUnit")) {
								line.remove("TaxPerUnit");
							}
							if (line.containsKey("TotalInclTax")) {
								line.remove("TotalInclTax");
							}
							if (line.containsKey("GrossPrice")) {
								line.remove("GrossPrice");
							}
							if (line.containsKey("GrossTotal")) {
								line.remove("GrossTotal");
							}
							if (line.containsKey("GrossTotalFC")) {
								line.remove("GrossTotalFC");
							}
							if (line.containsKey("GrossTotalSC")) {
								line.remove("GrossTotalSC");
							}
							if (line.containsKey("LineTaxJurisdictions")) {
								line.remove("LineTaxJurisdictions");
							}
							if (line.containsKey("BaseEntry")) {
								line.remove("BaseEntry");
							}
							if (line.containsKey("BaseLine")) {
								line.remove("BaseLine");
							}
							if (line.containsKey("BaseOpenQuantity")) {
								line.remove("BaseOpenQuantity");
							}
							if (line.containsKey("BaseType")) {
								line.remove("BaseType");
							}
							newLines.add(line);
						}
						map.put("DocumentLines", newLines);
						map.put("destination", destinationAssigned);
						// System.out.println("Destination set: "+destination);
						// Guardo la orden de venta a la nueva lista
						list.add(map);
						LOG.info("JAVA.WorkUtils.295: Added an PurchaseOrder for destination " + destinationAssigned);
					}
				}
			}
			return list;
		} else {
			LOG.info("JAVA.WorkUtils.77: Document Splitting didnt execute.");
			return payload;
		}
	}

	public static HashMap<String, Object> removeWarehouseInfo(String which, HashMap<String, Object> item) {
		if (which.equals("Items")) {
			item.remove("ItemWarehouseInfoCollection");
			if (item.containsKey("ItemUnitOfMeasurementCollection")) {
				item.remove("ItemUnitOfMeasurementCollection");
			}
		}
		item.remove("AttachmentEntry");
		return item;
	}

	public static HashMap<String, Object> removeUselessInfo(String which, HashMap<String, Object> item) {
		// return removeIfContainsNull(item); doesnt work.
		// if (which.equals("Items")) {
		// item.remove("AttachmentEntry");
		// }
		return item;
	}

	// Recursive function to remove all fields that are null > they would be ignored
	// by SAP anyway
	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> removeIfContainsNull(HashMap<String, Object> item) {
		ArrayList<String> strs = new ArrayList<String>();
		for (String str : item.keySet()) {
			if (item.get(str).getClass().getName().contains("List")) {
				for (HashMap<String, Object> map : (ArrayList<HashMap<String, Object>>) item.get(str)) {
					item.put(str, removeIfContainsNull(map));
				}
			} else if (item.get(str).getClass().getName().contains("Map")) {
				item.put(str, removeIfContainsNull((HashMap<String, Object>) item.get(str)));
			} else {
				if (item.get(str).toString().equals("null")) {
					strs.add(str);
				} else {
					// Nothing, its okay
				}
			}
		}
		for (String str : strs) {
			item.remove(str);
		}
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
				if (a < list.size()) {
					if (list.get(a) != null) {
						list.remove(a);
					}
				}
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

	public static ArrayList<HashMap<String, Object>> fetchStockForSocieties(String[] soc, ODBCManager manager,
			String codigo) throws SQLException {
		ArrayList<HashMap<String, Object>> lines = new ArrayList<>();
		for (String str : soc) {
			manager.createStatement();

			String almacenes = str + "_*";
			ResultSet set = manager.executeQuery(
					"SELECT (\"OnHand\" + \"OnOrder\" - \"IsCommited\") AS \"Cantidad\",\"ItemCode\", \"WhsCode\" FROM JMCG_DEV2.OITW WHERE (\"OnHand\" + \"OnOrder\" - \"IsCommited\") > 0 AND CONTAINS(\"WhsCode\", "
							+ almacenes + ")");

			// ResultSet set = manager.executeQuery("SELECT \"ItemCode\", \"WhsCode\",
			// \"OnHandQty\", \"BinAbs\" FROM "
			// + str + ".OIBQ WHERE \"OnHandQty\" > 0 AND \"Freezed\" = 'N' AND \"ItemCode\"
			// = '" + codigo + "'");
			while (set.next() != false) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("Sociedad", str);
				map.put("WhsCode", set.getString("WhsCode"));
				map.put("ItemCode", set.getString("ItemCode"));
				map.put("Quantity", set.getInt("Cantidad"));
				lines.add(map);
			}
		}

		return lines;
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
