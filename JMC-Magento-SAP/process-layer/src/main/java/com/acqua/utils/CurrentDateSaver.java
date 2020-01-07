package com.acqua.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

public class CurrentDateSaver {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	public static HashMap<String,String> UpdateDate = new HashMap<String,String>();

	public static HashMap<String, String> getUpdateTime(String request) throws IOException {
		HashMap<String, String> map = new HashMap<String, String>();
		LOG.info("JAVA.CurrentDateSaver.20: Retriving update date for "+request);
		if (UpdateDate.get(request) != null) {
			LOG.info("JAVA.CurrentDateSaver.22: Update date exists on memory, returning it");
			map.put("UpdateDate", UpdateDate.get(request));
			return map;
		}
		else 
		{
			LOG.info("JAVA.CurrentDateSaver.29: Update date is not on memory, look up file");
			// CurrentTimeSaver.class.getResource("/keystore.jks").getPath().split("keystore.jks")[0]+"DateTimeSaver.properties"
			File file = new File("CurrentDateSaver_"+request+".properties");
			//System.out.println(file.getCanonicalPath());
			if (!file.exists())
			{
			LOG.info("JAVA.CurrentDateSaver.35: File doesnt exist, creating it");
			file.createNewFile();
			}
			Properties prop = new Properties();
			LOG.info("JAVA.CurrentDateSaver.39: Initiating FileInputStream to "+"CurrentDateSaver_"+request+".properties");
			FileInputStream input = new FileInputStream("CurrentDateSaver_"+request+".properties");
			//System.out.println(input.available());
/*			int i;
			while((i=input.read()) != -1) {
				System.out.println((char) i);
			}*/
			LOG.info("JAVA.CurrentDateSaver.46: Loading properties from fileInputStream");
			prop.load(input);
			//System.out.println("Reading property "+prop.getProperty("UpdateTime."+request));
			UpdateDate.put(request, prop.getProperty("UpdateDate."+request));
			
			//Insurances
			LOG.info("JAVA.CurrentDateSaver.53: Check if dates are null, if so, set to 2001-01-01 00:00:00");
			if (UpdateDate.get(request) == null)
				UpdateDate.put(request,"2001-01-01");
				
			input.close();
			//System.out.println(prop.getProperty("UpdateTime."+request));
			LOG.info("JAVA.CurrentDateSaver.62: Return Date");
			map.put("UpdateDate", UpdateDate.get(request));
			return map;
		}
	}
	
	public static Boolean setUpdateTime(String request, String date) throws IOException, ParseException {
			LOG.info("JAVA.CurrentDateSaver.72: setUpdateTime Called for "+request);
			if (!isStringDateAfter(date, UpdateDate.get(request))) {
				LOG.info("Update Date "+date+" is not newer than existing "+UpdateDate.get(request));
				return false;
			}
			LOG.info("Updating update date of "+request+" to "+date);
			String correctDate = date;
			UpdateDate.put(request, correctDate);
			
			Properties prop = new Properties();
			LOG.info("JAVA.CurrentDateSaver.75: Create File OutputStream to "+"CurrentDateSaver_"+request+".properties");
			FileOutputStream output = new FileOutputStream("CurrentDateSaver_"+request+".properties");
			prop.setProperty("UpdateDate."+request, correctDate);
			LOG.info("JAVA.CurrentDateSaver.79: Store properties to file");
			prop.store(output, "");
			output.close();
		return true;
	}
	
	public static Boolean isStringDateAfter(String date1, String date2) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date dateObj1 = sdf.parse(date1);
		Calendar calendar1 = new GregorianCalendar();
		calendar1.setTime(dateObj1);
		Date dateObj2 = sdf.parse(date2);
		Calendar calendar2 = new GregorianCalendar();
		calendar2.setTime(dateObj2);
		
		return calendar1.after(calendar2);
	}
}
