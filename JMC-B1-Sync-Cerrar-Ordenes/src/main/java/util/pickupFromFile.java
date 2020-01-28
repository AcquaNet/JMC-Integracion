package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class pickupFromFile extends AbstractMessageTransformer {
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		Integer orden = message.getInvocationProperty("orden");
		ArrayList<String> sociedades = message.getInvocationProperty("sociedades");
		String info = pickupFromFile(orden);
		if (info != null) {
		return info;
		}
		else
		{
			return "{\"orden\":"+orden+", \"sociedades\":[\"inexistente\"], \"completadas\":[]}";
		}
	}

	public String pickupFromFile(Integer orden) {
		// Check if main directory exists, otherwise create
		File directorioPrincipal = new File("ordenStatus/");
		if (!directorioPrincipal.exists()) {
			return null;
		}

//		// Check if society directory exists, otherwise create
//		File directorioSociedad = new File("ordenStatus/" + date + "/");
//		if (!directorioSociedad.exists()) {
//			return null;
//		}

		// Check if file exists
		String fileName = "ordenStatus/OV_" + orden + ".json";
		File file = new File(fileName);
		System.out.println(file.getAbsolutePath());
		if (!file.exists()) {
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

				// Merge data
				return existingJSON;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

}
