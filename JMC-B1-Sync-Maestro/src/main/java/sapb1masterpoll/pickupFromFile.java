package sapb1masterpoll;

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
		String date = (String) message.getInvocationProperty("fileDate");
		String time = (String) message.getInvocationProperty("fileTime");
		String info = pickupFromFile(date, time);
		return info;
	}

	public String pickupFromFile(String date, String time) {
		// Check if main directory exists, otherwise create
		File directorioPrincipal = new File("errorReporting/");
		if (!directorioPrincipal.exists()) {
			return null;
		}

		// Check if society directory exists, otherwise create
		File directorioSociedad = new File("errorReporting/" + date + "/");
		if (!directorioSociedad.exists()) {
			return null;
		}

		// Check if file exists
		String fileName = "errorReporting/" + date + "/" + time + ".json";
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
