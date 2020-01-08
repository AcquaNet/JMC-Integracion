package acqua.util.Transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import acqua.util.JSONUtil;

public class ConsolidarRecuentoEntradaMercancia extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh");

	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		
		LOG.info("ConsolidarRecuentoEntradaMercancia class called.");
		// First we load in all payload information
		HashMap<String, Object> payload = (HashMap<String, Object>) message.getPayload();

		// Then we parse all important information only (saving space)
		ArrayList<Object> items = new ArrayList<>();

		for (HashMap<String, Object> map : (ArrayList<HashMap<String, Object>>) payload.get("listaDeArticulos")) {
			HashMap<String, Object> newMap = new HashMap<>();
			newMap.put("codigo", map.get("codigo"));
			newMap.put("cantidad", map.get("cantidad"));
			items.add(newMap);
		}

		// Get information for file
		String sociedad = (String) payload.get("sociedad");
		String operacion = (String) payload.get("codigobarra");

		// Check if main directory exists, otherwise create
		File directorioPrincipal = new File("entradasMercancia/");
		if (!directorioPrincipal.exists()) {
			directorioPrincipal.mkdir();
			LOG.info("ConsolidarRecuentoEntradaMercancia: entradasMercancia mkdir");
		}
		

		// Check if society directory exists, otherwise create
		File directorioSociedad = new File("entradasMercancia/" + sociedad + "/");
		if (!directorioSociedad.exists()) {
			directorioSociedad.mkdir();
			LOG.info("ConsolidarRecuentoEntradaMercancia: entradasMercancia/"+sociedad+" mkdir");
		}

		// Check if file exists
		String fileName = "entradasMercancia/" + sociedad + "/" + operacion + ".json";
		File file = new File(fileName);
		if (!file.exists()) {
			// If it doesnt...
			LOG.info("ConsolidarRecuentoEntradaMercancia: file "+fileName+" doesnt exist. Creating.");
			try {
				file.createNewFile();
				// Convert Items to write to JSON
				HashMap<String, Object> mapToSave = new HashMap<String, Object>();
				mapToSave.put("data", items);
				String json = acqua.util.JSONUtil.javaToJSONToString(mapToSave);
				FileOutputStream outputStream = new FileOutputStream(file, false);
				byte[] strToBytes = json.getBytes();
				outputStream.write(strToBytes);
				outputStream.close();
				LOG.info("ConsolidarRecuentoEntradaMercancia: file created and filled. Returning true.");
				LOG.info("ConsolidarRecuentoEntradaMercancia: file aboslute path: "+file.getAbsolutePath());
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// If it already exists
			LOG.info("ConsolidarRecuentoEntradaMercancia: file "+fileName+" exists. Reading.");
			try {
				// Read exisitng file info
				FileInputStream existingFile = new FileInputStream(file);
				String existingJSON = "";
				int content;
				while ((content = existingFile.read()) != -1) {
					// convert to char and append
					existingJSON = existingJSON + (char) content;
					// System.out.print((char) content);
				}
				LOG.info("ConsolidarRecuentoEntradaMercancia: file "+fileName+" content recovered.");

				existingFile.close();
				// Convert JSON to Map and return data
				HashMap<String, Object> inputMap = (HashMap<String, Object>) acqua.util.JSONUtil
						.stringToMap(existingJSON);
				// Convert JSON to usable ArrayList
				ArrayList<HashMap<String, Object>> existingItems = (ArrayList<HashMap<String, Object>>) inputMap
						.get("data");

				// Set empty ArrayList for later
				ArrayList<Object> listForSaving = new ArrayList<>();

				// Merge data
				for (Object map : items) {
					HashMap<String, Object> castMap = (HashMap<String, Object>) map;
					Boolean existing = false;
					int i = 0;
					for (HashMap<String, Object> existingMap : existingItems) {
						if (castMap.get("codigo").equals(existingMap.get("codigo"))) {
							castMap.put("cantidad", (Double) existingMap.get("cantidad") + (Double) castMap.get("cantidad"));
							listForSaving.add(castMap);
							existing = true;
							break;

						}
						i++;
					}
					if (existing) {
						existingItems.remove(i);
					} else {
					listForSaving.add(castMap);
					}
				}
				listForSaving.addAll(existingItems);

				// Once done, convert back to JSON
				HashMap<String, Object> mapToSave = new HashMap<String, Object>();
				mapToSave.put("data", listForSaving);
				String json = acqua.util.JSONUtil.javaToJSONToString(mapToSave);
				
				LOG.info("ConsolidarRecuentoEntradaMercancia: Writing JSON to file: "+json);
				// Write to file
				try {
					file.createNewFile();
					// Convert Items to write to JSON
					FileOutputStream outputStream = new FileOutputStream(file, false);
					byte[] strToBytes = json.getBytes();
					outputStream.write(strToBytes);
					outputStream.close();
					LOG.info("ConsolidarRecuentoEntradaMercancia: file is done and closed. Returning true.");
					LOG.info("ConsolidarRecuentoEntradaMercancia: file aboslute path: "+file.getAbsolutePath());
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		LOG.info("ConsolidarRecuentoEntradaMercancia: Returning false.");
		return false;
	}

}
