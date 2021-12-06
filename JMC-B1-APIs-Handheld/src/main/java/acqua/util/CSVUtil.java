package acqua.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;

import org.apache.log4j.Logger;
import acqua.util.Transform.TestPojo;

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

	public static List<TestPojo> csvReader (String csvData){
		
		CsvSchema headerSchema = CsvSchema.emptySchema().withHeader();
		final CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.TRIM_SPACES);
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		
		List<TestPojo> lines = new ArrayList<>();
		
		try {
			MappingIterator<TestPojo> it = mapper
					  .readerFor(TestPojo.class)
					  .with(headerSchema)
					  .readValues(csvData);
			
			while (it.hasNext()) {
				TestPojo current = it.next();
				lines.add(current);
				System.out.println(current);
			}
			
			return lines;
		}  catch (Exception e) {
			
			System.out.println("Failed: " + e.getMessage());
			LOG.error("CSVReader Failed: " + e.getMessage());
			e.printStackTrace();
		}

		return null;
	}
}
