package acqua.util.Transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;


public class LeerRecuentoEntradaMercancia extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh.log");

	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Get information for file
		String sociedad = (String) message.getInvocationProperty("sociedad");
		String operacion = (String) message.getInvocationProperty("codigo");

		// Check if main directory exists, otherwise create
		File directorioPrincipal = new File("entradasMercancia/");
		if (!directorioPrincipal.exists()) {
			directorioPrincipal.mkdir();
		}

		// Check if society directory exists, otherwise create
		File directorioSociedad = new File("entradasMercancia/" + sociedad + "/");
		if (!directorioSociedad.exists()) {
			directorioSociedad.mkdir();
		}

		// Check if file exists
		String fileName = "entradasMercancia/" + sociedad + "/" + operacion + ".json";
		File file = new File(fileName);
		if (!file.exists()) {
			// If it doesnt...
				LOG.info("No existe el archivo "+fileName);
				return null;
		} else {
			// If it already exists
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

				existingFile.close();
				//file.delete(); No queremos borrarlo al instante
				
				// Convert JSON to Map and return data
				HashMap<String, Object> inputMap = (HashMap<String, Object>) acqua.util.JSONUtil
						.stringToMap(existingJSON);
				if (inputMap == null) {
					return null;
				}
				// Convert JSON to usable ArrayList
				ArrayList<HashMap<String, Object>> existingItems = (ArrayList<HashMap<String, Object>>) inputMap
						.get("data");
				if (existingItems.size() == 0) {
					return null;
				}
				return existingItems;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return null;
	}
	
}
