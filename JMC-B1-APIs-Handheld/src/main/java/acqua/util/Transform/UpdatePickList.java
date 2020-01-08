package acqua.util.Transform;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.ODBCManager;

public class UpdatePickList extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		ArrayList<HashMap<String, Object>> inputArticulos = message.getInvocationProperty("listaDeArticulos");
		HashMap<String, Object> pickList = (HashMap<String, Object>) message.getPayload();
		HashMap<Integer, Integer> BinAbsList = (HashMap<Integer, Integer>) message.getInvocationProperty("PickListAbs");
		HashMap<String, Object> pickListCopy = new HashMap<>();
		ArrayList<HashMap<String, Object>> pickLines = (ArrayList<HashMap<String, Object>>) pickList
				.get("PickListsLines");
		ArrayList<HashMap<String, Object>> pickLinesCopy = new ArrayList<>();
		for (HashMap<String, Object> itemLine : inputArticulos) {
			Integer newCount;
			// System.out.println("ItemLine" + itemLine.get("linenum"));
			for (HashMap<String, Object> originalLine : pickLines) {
				// System.out.println("Original Line Num " + originalLine.get("LineNumber"));
				if (itemLine.get("linenum").equals(originalLine.get("LineNumber"))) {
					// System.out.println("True in Loop");
					if (!originalLine.get("PickStatus").equals("ps_Closed")) {
						double cantidadRecibida = (double) itemLine.get("cantidad");
						newCount = (int) cantidadRecibida;
						Integer LineQtty = ((int) ((double) originalLine.get("PickedQuantity"))) + newCount;
						originalLine.put("PickedQuantity", LineQtty);
						ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) originalLine
								.get("BatchNumbers");
						ArrayList<HashMap<String, Object>> listCopy = new ArrayList<>();
						int a = 0;
						for (HashMap<String, Object> map : list) {
							map.remove("ManufacturerSerialNumber");
							map.remove("InternalSerialNumber");
							map.remove("ExpiryDate");
							map.remove("ManufacturingDate");
							map.remove("TrackingNote");
							map.remove("TrackingNoteLine");
							map.remove("SerialNumbers");
							map.remove("Notes");
							map.put("Quantity", (Integer) map.get("Quantity") + newCount);
							if (a < 1) {
								listCopy.add(map);
							}
							a++;
						}

						// Update also allocations, only will insert into first
						ArrayList<HashMap<String, Object>> AllocationList = (ArrayList<HashMap<String, Object>>) originalLine
								.get("DocumentLinesBinAllocations");
						ArrayList<HashMap<String, Object>> AllocationListCopy = new ArrayList<>();
						HashMap<String, Object> backupAbs = new HashMap<>();
						backupAbs.put("BinAbsEntry", BinAbsList.get((Integer) originalLine.get("LineNumber")));
						backupAbs.put("Quantity", LineQtty);
						backupAbs.put("AllowNegativeQuantity", "tNO");
						backupAbs.put("BaseLineNumber", originalLine.get("LineNumber"));
						if (a > 0) {
							backupAbs.put("SerialAndBatchNumbersBaseLine", 0);
						} else if (a == 0) {
							backupAbs.put("SerialAndBatchNumbersBaseLine", -1);
						}

						AllocationListCopy.add(backupAbs);
						originalLine.put("BatchNumbers", listCopy);
						originalLine.put("DocumentLinesBinAllocations", AllocationListCopy);
					}
					// originalLine.remove("DocumentLinesBinAllocations");
					pickLinesCopy.add(originalLine);
				}

			}
		}
		// Updated Object count
		pickListCopy.put("PickListsLines", pickLinesCopy);
		// Must have
		pickListCopy.put("Status", pickList.get("Status"));
		pickListCopy.put("ObjectType", pickList.get("ObjectType"));
		pickListCopy.put("Absoluteentry", pickList.get("Absoluteentry"));
		pickListCopy.put("PickDate", pickList.get("PickDate"));
		pickListCopy.put("UseBaseUnits", pickList.get("UseBaseUnits"));

		return pickListCopy;
	}

	// {
	// "Status": "ps_Picked",
	// "ObjectType": "156",
	// "PickListsLines": [
	// {
	// "OrderEntry": 397,
	// "AbsoluteEntry": 106,
	// "PreviouslyReleasedQuantity": 10.0,
	// "PickedQuantity": 2.0,
	// "PickStatus": "ps_Picked",
	// "SerialNumbers": [],
	// "BatchNumbers": [
	// {
	// "BaseLineNumber": 0,
	// "AddmisionDate": "2019-04-29",
	// "BatchNumber": "OP123",
	// "Quantity": 2.0,
	//
	// "Location": ""
	// }
	// ],
	// "ReleasedQuantity": 8.0,
	// "OrderRowID": 0,
	// "LineNumber": 0,
	// "BaseObjectType": 17,
	// "DocumentLinesBinAllocations": [
	// {
	// "BaseLineNumber": 0,
	// "SerialAndBatchNumbersBaseLine": 0,
	// "Quantity": 2.0,
	// "AllowNegativeQuantity": "tNO",
	// "BinAbsEntry": 5836
	// }
	// ]
	// }
	// ],
	// "UseBaseUnits": "tNO",
	// "Absoluteentry": 106,
	// "PickDate": "2019-04-29"
	// }
	//
}
