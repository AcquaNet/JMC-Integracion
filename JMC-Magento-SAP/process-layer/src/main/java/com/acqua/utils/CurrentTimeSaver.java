package com.acqua.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;

public class CurrentTimeSaver {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	public static HashMap<String,String> UpdateTime = new HashMap<String,String>();

	public static HashMap<String, String> getUpdateTime(String request) throws IOException {
		HashMap<String, String> map = new HashMap<String, String>();
		LOG.info("JAVA.CurrentTimeSaver.20: Retriving update time for "+request);
		if (UpdateTime.get(request) != null) {
			LOG.info("JAVA.CurrentTimeSaver.22: Update time exists on memory, returning it");
			map.put("UpdateTime", UpdateTime.get(request));
			return map;
		}
		else 
		{
			LOG.info("JAVA.CurrentTimeSaver.29: Update date is not on memory, look up file");
			// CurrentTimeSaver.class.getResource("/keystore.jks").getPath().split("keystore.jks")[0]+"DateTimeSaver.properties"
			File file = new File("CurrentTimeSaver_"+request+".properties");
			//System.out.println(file.getCanonicalPath());
			if (!file.exists())
			{
			LOG.info("JAVA.CurrentTimeSaver.35: File doesnt exist, creating it");
			file.createNewFile();
			}
			Properties prop = new Properties();
			LOG.info("JAVA.CurrentTimeSaver.39: Initiating FileInputStream to "+"CurrentTimeSaver_"+request+".properties");
			FileInputStream input = new FileInputStream("CurrentTimeSaver_"+request+".properties");
			//System.out.println(input.available());
/*			int i;
			while((i=input.read()) != -1) {
				System.out.println((char) i);
			}*/
			LOG.info("JAVA.CurrentTimeSaver.46: Loading properties from fileInputStream");
			prop.load(input);
			//System.out.println("Reading property "+prop.getProperty("UpdateTime."+request));
			UpdateTime.put(request, prop.getProperty("UpdateTime."+request));
			
			//Insurances
			LOG.info("JAVA.CurrentTimeSaver.53: Check if time is null, if so, set to 0000");
			if (UpdateTime.get(request) == null)
				UpdateTime.put(request,"0000");
				
			input.close();
			//System.out.println(prop.getProperty("UpdateTime."+request));
			LOG.info("JAVA.CurrentTimeSaver.62: Return Time");
			map.put("UpdateTime", UpdateTime.get(request));
			return map;
		}
	}
	
	public static Boolean setUpdateTime(String request, String time) throws IOException, ParseException {
			LOG.info("JAVA.CurrentTimeSaver.72: setUpdateTime Called for "+request);

			LOG.info("Updating update date of "+request+" to "+time);
			String correctTime = time;
			UpdateTime.put(request, correctTime);
			
			Properties prop = new Properties();
			LOG.info("JAVA.CurrentTimeSaver.75: Create File OutputStream to "+"CurrentTimeSaver_"+request+".properties");
			FileOutputStream output = new FileOutputStream("CurrentTimeSaver_"+request+".properties");
			prop.setProperty("UpdateTime."+request, correctTime);
			LOG.info("JAVA.CurrentTimeSaver.79: Store properties to file");
			prop.store(output, "");
			output.close();
		return true;
	}
}