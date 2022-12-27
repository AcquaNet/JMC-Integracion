package acqua.util.Transform;

import java.util.ArrayList;
import java.util.HashMap;

public class ConsolidarCurvasArticulos {

	public static HashMap<String, Object> recuperarCurvasArticulos(
			ArrayList<HashMap<String, Object>> listaHHArticulos) {

		ArrayList<HashMap<String, Object>> listaCurvas = new ArrayList<HashMap<String, Object>>();
		ArrayList<HashMap<String, Object>> listaArticulos = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < listaHHArticulos.size(); i++) {
			if ((Boolean) listaHHArticulos.get(i).get("Curva")) {
				listaCurvas.add(listaHHArticulos.get(i));
			} else {
				listaArticulos.add(listaHHArticulos.get(i));
			}
		}
		HashMap<String, Object> response = new HashMap<String, Object>();
		response.put("listaCurvas", listaCurvas);
		response.put("listaArticulos", listaArticulos);

		return response;
	}

	public static String armarCodigosCurvas(ArrayList<HashMap<String, Object>> listaCurvas) {

		String codigosCurva = "";

		for (int i = 0; i < listaCurvas.size(); i++) {
			if (codigosCurva.isEmpty()) {
				codigosCurva = (String) listaCurvas.get(i).get("ItemCode");
			} else {
				codigosCurva = codigosCurva + "," + (String) listaCurvas.get(i).get("ItemCode");
			}
		}

		return codigosCurva;
	}

	public static Object consolidarCurvas(HashMap<String, ArrayList<Object>> itemsCurva,
			ArrayList<HashMap<String, Object>> listaArticulos, ArrayList<HashMap<String, Object>> listaCurvas) {

		HashMap<String, Integer> curvas = new HashMap<String, Integer>();

		ArrayList<HashMap<String, Object>> curvasCompletas = new ArrayList<HashMap<String, Object>>();
		ArrayList<HashMap<String, Object>> curvasIncompletas = new ArrayList<HashMap<String, Object>>();

		HashMap<String, ArrayList<Object>> itemsFaltantes = new HashMap<String, ArrayList<Object>>();

		for (int i = 0; i < listaCurvas.size(); i++) {
			String codigoCurva = (String) listaCurvas.get(i).get("ItemCode");
			int cantCurvas = (Integer) listaCurvas.get(i).get("Cant");

			if (!curvas.containsKey(codigoCurva)) {
				curvas.put(codigoCurva, 0);
			}

			ArrayList<Object> elementosCurva = (ArrayList<Object>) itemsCurva.get(codigoCurva);

			for (int a = 0; a < cantCurvas; a++) {
				Boolean itemsDone = true;
				ArrayList<HashMap<String, Object>> itemsToUpdate = new ArrayList<HashMap<String, Object>>();
				for (int u = 0; u < elementosCurva.size(); u++) {

					String codigoItem = (String) ((HashMap<String, Object>) elementosCurva.get(u)).get("item");
					Double cant = (Double) ((HashMap<String, Object>) elementosCurva.get(u)).get("cantidad");

					for (int e = 0; e < listaArticulos.size(); e++) {
						if (listaArticulos.get(e).get("ItemCode").equals(codigoItem)) {
							Double cantRecibida = Double.valueOf((int) listaArticulos.get(e).get("Cant"));
							Double itemQuantity = cant - cantRecibida;
							
							
							if (itemQuantity <= 0) {
								HashMap<String, Object> itemToUpdate = new HashMap<String, Object>();
								itemToUpdate.put("index", e);
								itemToUpdate.put("newValue", (int) (cantRecibida - cant));
								itemsToUpdate.add(itemToUpdate);
								
							} else {
								HashMap<String, Object> itemToUpdate = new HashMap<String, Object>();
								itemToUpdate.put("index", e);
								itemToUpdate.put("newValue", 0);
								itemsToUpdate.add(itemToUpdate);

								ArrayList<Object> itemCurvaArray = new ArrayList<Object>();
								HashMap<String, Object> itemCurva = new HashMap<String, Object>();
								itemCurva.put("ItemCode", codigoItem);
								itemCurva.put("CantFaltante", itemQuantity);
								itemCurva.put("Curva", codigoCurva);
								itemCurvaArray.add(itemCurva);

								if (!itemsFaltantes.containsKey(codigoCurva)) {
									itemsFaltantes.put(codigoCurva, itemCurvaArray);
								} else {
									ArrayList<Object> itemsCurvaArray = itemsFaltantes.get(codigoCurva);
									itemsCurvaArray.addAll(itemCurvaArray);
									itemsFaltantes.put(codigoCurva, itemsCurvaArray);
								}

								itemsDone = false;
							}
							break;
						}
					}
				}
				if (itemsDone) {
					
					for (int e=0; e < itemsToUpdate.size(); e++) {
						int index = (int) itemsToUpdate.get(e).get("index");
						listaArticulos.get(index).put("Cant", itemsToUpdate.get(e).get("newValue"));
					}
					
					curvas.put(codigoCurva, curvas.get(codigoCurva) + 1);
				}
			}
			
			int totalCurvas = curvas.get(codigoCurva);
			
			if (cantCurvas == totalCurvas) {
				curvasCompletas.add(listaCurvas.get(i));
			} else {
				listaCurvas.get(i).put("CantRecibida", totalCurvas);
				curvasIncompletas.add(listaCurvas.get(i));
			}

		}

		HashMap<String, Object> finalCurvas = new HashMap<String, Object>();
		finalCurvas.put("itemsFaltantes", itemsFaltantes);
		finalCurvas.put("listaArticulos", listaArticulos);
		finalCurvas.put("curvasCompletas", curvasCompletas);
		finalCurvas.put("curvasIncompletas", curvasIncompletas);

		return finalCurvas;
	}

}
