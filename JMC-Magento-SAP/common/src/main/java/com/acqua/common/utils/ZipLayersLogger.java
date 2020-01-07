package com.acqua.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipLayersLogger implements Callable {

	private Logger logger = LoggerFactory.getLogger(CommonLayer.class);

	private String loggersDir = "";

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		logger.debug("ZipLayersLogger BEGIN");

		logger.debug("Logging Folder: " + getClass().getClassLoader().getResource("log4j2.xml"));

		MuleMessage message = eventContext.getMessage();

		// GET Mule Home

		String mule_home = "";

		if (loggersDir.isEmpty()) {

			logger.debug("Mule Message: " + message.toString());

			mule_home = message.getProperty("mule_home", PropertyScope.INVOCATION);

			loggersDir = mule_home.concat(File.separator).concat("logs");

		}

		logger.debug("Mule Home: " + mule_home);
		logger.debug("Mule Message: " + loggersDir);

		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
		String date = sdf.format(new Date());

		try {

			File tempZip = File.createTempFile("LOGS", ".zip");
			FileOutputStream fos = new FileOutputStream(tempZip);
			ZipOutputStream zipOut = new ZipOutputStream(fos);

			File logDir = new File(loggersDir);

			if (logDir != null) {

				logger.debug("    Processing Log Folder: " + logDir.getAbsolutePath());
				
				zipOut.setMethod(ZipOutputStream.DEFLATED);
				zipOut.setLevel(9);
				FileInputStream in = null;
				int len = 0;
				byte[] buf = new byte[1024];
				
				for (File file : logDir.listFiles()) {
					 
					long millis = file.lastModified();
					
					String fileDate = sdf.format(millis);

					if (fileDate.equals(date)) {
						
						String filename = file.getName();
						
						in = new FileInputStream(file);
						
						
						zipOut.putNextEntry(new ZipEntry(filename));
						
						while ((len = in.read(buf)) > 0) {
							zipOut.write(buf, 0, len);
						}
						
						zipOut.closeEntry();
						
						logger.debug("         File: " + file.getName() + " zipped");
						
						in.close();
					}
					else
					{
						logger.debug("         File: " + file.getName() + " not zipped");
						
					}

				}

			}
			
			zipOut.close();

			FileDataSource attachment = new FileDataSource(tempZip.getAbsoluteFile());

			message.addOutboundAttachment("zipLoggers" + date + ".zip", new DataHandler(attachment));
			
			message.setInvocationProperty("isThereAttachment", true);

		} catch (Exception e) {

			logger.error("ZipLayersLogger. Error: " + e.getMessage(),e);
			
			message.setInvocationProperty("isThereAttachment", false);
			
		}

		logger.debug("ZipLayersLogger END");

		return message;

	}

}