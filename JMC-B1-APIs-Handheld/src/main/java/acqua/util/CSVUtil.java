package acqua.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;

import org.apache.log4j.Logger;

public class CSVUtil {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	public static void csvWriter(String csvData, String path, String fileName) {
		
		try {
			JsonNode jsonTree = new ObjectMapper().readTree(csvData);
			
			Builder csvSchemaBuilder = CsvSchema.builder();
			JsonNode firstObject = jsonTree.elements().next();
			firstObject.fieldNames().forEachRemaining(fieldName -> {csvSchemaBuilder.addColumn(fieldName);} );
			CsvSchema csvSchema = csvSchemaBuilder.build()
									.withHeader()
									.withColumnSeparator(';')
									.withNullValue("")
									.withoutQuoteChar();
			
			CsvMapper csvMapper = new CsvMapper();
			String fileTimestamp = new SimpleDateFormat("_yyyyMMdd_HHmmssSSS").format(new Date());
			
			
			csvMapper.writerFor(JsonNode.class).with(csvSchema).writeValue(new File(path + "/" + fileName + fileTimestamp + ".csv"), jsonTree);
			
		} catch (Exception e) {

			System.out.println("Failed: " + e.getMessage());
			LOG.error("CSVWriter Failed: " + e.getMessage());
		} 
	}

}
