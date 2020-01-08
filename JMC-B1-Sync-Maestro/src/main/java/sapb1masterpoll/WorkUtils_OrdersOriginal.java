package sapb1masterpoll;
//// Lista en blanco
//			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
//			// Loopeo por cada orden en el payload
//			for (HashMap<String, Object> map : payload) {
//				// Lineas del documento
//				ArrayList<HashMap<String, Object>> lines = (ArrayList<HashMap<String, Object>>) map
//						.get("DocumentLines");
//				// Saco las lineas despues de guardarlas al arraylist para dejar un documento
//				// limpio
//				LOG.info("JAVA.WorkUtils.89: DocNum of this document being processed: " + map.get("DocNum"));
//
//				// Creo un mapa para guardar ArrayList de lineas de cada destino
//				HashMap<String, Object> possibleMaps = new HashMap<String, Object>();
//				// Loopeo por todas las lineas
//				if (lines == null) {
//
//					FatalLog.info("JAVA.WorkUtils.157: Document lines are null?? ERROR");
//					FatalLog.info("JAVA.WorkUtils.158: Document with issues: " + JSONUtil.javaToJSONToString(map));
//					// return null;
//				} else {
//
//					for (HashMap<String, Object> lineMap : lines) {
//						// Obtengo el nobmre del destino correcto en base al codigo de almacen
//						// System.out.println("LineMap destination WH: "+lineMap.get("WarehouseCode"));
//						String destinationAssigned = destinationMap.get((String) lineMap.get("WarehouseCode"));
//						// System.out.println("DestinationMap values");
//						// Todas las lineas van al almacen principal, 01
//						lineMap.put("WarehouseCode", "01");
//
//						// Solo sigo si la linea no est� cerrada.
//						if (!lineMap.get("LineStatus").equals("bost_Close")) {
//
//							// Reviso si ya hay lineas para el destino
//
//							if (possibleMaps.containsKey(destinationAssigned)
//									&& possibleMaps.get(destinationAssigned) != null) {
//								// System.out.println("Assigned destination: "+destinationAssigned);
//								// Si las hay, obtengo el array
//								ArrayList<HashMap<String, Object>> ListOfLines = (ArrayList<HashMap<String, Object>>) possibleMaps
//										.get(destinationAssigned);
//								// Agrego la linea a ese array
//								ListOfLines.add(lineMap);
//								// Guardo la lista al hashmap
//								possibleMaps.put(destinationAssigned, ListOfLines);
//							}
//							// Si todavia no hay lineas para el destino;
//							else {
//								// Creo el array list
//								ArrayList<HashMap<String, Object>> ListOfLines = new ArrayList<HashMap<String, Object>>();
//								// Agrego la linea a ese array
//								ListOfLines.add(lineMap);
//								// Guardo la lista al hashmap
//								possibleMaps.put(destinationAssigned, ListOfLines);
//							}
//						}
//					}
//					LOG.info("JAVA.WorkUtils.128: Ammount of destinations: " + possibleMaps.keySet().size());
//					// Loopeo por cada lista de lineas (segun destinacion)
//					for (String destination : possibleMaps.keySet()) {
//
//						// Obtengo la lista
//						ArrayList<HashMap<String, Object>> documentLines = (ArrayList<HashMap<String, Object>>) possibleMaps
//								.get(destination);
//
//						// Contador de Lineas
//						int i = 1;
//						// Loopeo por cada linea para arreglar la numeraci�n
//						for (HashMap<String, Object> lineCurrentMap : documentLines) {
//							// Arreglo la linea
//							lineCurrentMap.put("LineNum", i);
//							// Si la lista de tax existe, tambien hay que arreglarla internamente
//							if (lineCurrentMap.get("LineTaxJurisdictions").getClass().getName().contains("List")) {
//								// Loopeo por cada linea de la lista de tax
//								for (HashMap<String, Object> jurisdictionMapLine : (ArrayList<HashMap<String, Object>>) lineCurrentMap
//										.get("LineTaxJurisdictions")) {
//									// Arreglo la linea referenciada "pariente" de tax.
//									jurisdictionMapLine.put("LineNumber", i);
//								}
//							}
//							i++;
//						}
//						if (lines != null) {
//							LOG.info("JAVA.WorkUtils.90: Ammount of lines:" + lines.size());
//							map.remove("DocumentLines");
//						}
//						// Remuevo datos de valor total para que lo re-calcule SAP
//						map.remove("DocTotal");
//						map.remove("VatSum");
//						map.remove("VatSumSys");
//
//						// Hago un clon del documento vacio sin documentLines
//						HashMap<String, Object> mapClone = (HashMap<String, Object>) map.clone();
//						// Le guardo las lineas correspondientes
//
//						mapClone.put("DocumentLines", documentLines);
//						// Guardo el destino de esta orden para filtrar despues en el forEach
//						mapClone.put("destination", destination);
//						// System.out.println("Destination set: "+destination);
//						// Guardo la orden de venta a la nueva lista
//						list.add(mapClone);
//						LOG.info("JAVA.WorkUtils.143: Added an order for destination " + destination);
//					}
//				}
//			}
//			return list;