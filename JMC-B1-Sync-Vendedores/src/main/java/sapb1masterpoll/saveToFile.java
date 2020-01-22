package sapb1masterpoll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class saveToFile extends AbstractMessageTransformer {
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		String date = (String) message.getInvocationProperty("fileDate");
		String time = (String) message.getInvocationProperty("fileTime");
		String mail = null;
		try {
			mail = message.getPayloadAsString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		saveToFile(mail, date, time);
		return null;
	}
	
	
	public void saveToFile(String str, String date, String time) {
		// Check if main directory exists, otherwise create
		File directorioPrincipal = new File("errorReporting_SyncVendedores/");
		if (!directorioPrincipal.exists()) {
			directorioPrincipal.mkdir();
		}

		// Check if society directory exists, otherwise create
		File directorioSociedad = new File("errorReporting_SyncVendedores/" + date + "/");
		if (!directorioSociedad.exists()) {
			directorioSociedad.mkdir();
		}
		
		// Check if file exists
				String fileName = "errorReporting_SyncVendedores/" + date + "/" + time + ".json";
				File file = new File(fileName);
				//System.out.println(file.getAbsolutePath());
				if (!file.exists()) {
					// If it doesnt...
					try {
						file.createNewFile();
						// write to JSON

						String json = str;
						FileOutputStream outputStream = new FileOutputStream(file, false);
						byte[] strToBytes = json.getBytes();
						outputStream.write(strToBytes);
						outputStream.close();

					} catch (IOException e) {
						e.printStackTrace();
					}
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
						String json = existingJSON + "\n"+str;

						// Write to file
						try {
							file.createNewFile();
							// Convert Items to write to JSON
							FileOutputStream outputStream = new FileOutputStream(file, false);
							byte[] strToBytes = json.getBytes();
							outputStream.write(strToBytes);
							outputStream.close();

						} catch (IOException e) {
							e.printStackTrace();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				}
	}
	

	public static String getCurrentTime() {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		String timeString = "ErrorFile_"+String.format("%02d",calendar.get(Calendar.HOUR_OF_DAY))+"-"+String.format("%02d",calendar.get(Calendar.MINUTE))+"-"+String.format("%02d",calendar.get(Calendar.SECOND));
		return timeString;
	}
	

	public static String getCurrentDate() {
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		String timeString = ""+calendar.get(Calendar.YEAR)+"-"+String.format("%02d",calendar.get(Calendar.MONTH))+"-"+String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH));
		return timeString;
	}
}
