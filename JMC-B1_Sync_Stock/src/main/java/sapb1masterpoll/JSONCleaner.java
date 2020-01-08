package sapb1masterpoll;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONCleaner extends AbstractMessageTransformer {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		LOG.info("JSONCleaner.20: Invoked");
		 ObjectMapper mapper = new ObjectMapper();


			String payload = null;
			try {
				payload = message.getPayloadAsString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, Object> map = null;
	        try {
	        	map = mapper.readValue(payload, new TypeReference<Map<String, Object>>() {
	            });
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

			HashMap<String,Object> answer = new HashMap<>();
			answer = removeEmptyMaps(map, "");
			String answerPayload = null;
	        try {
	            answerPayload = mapper.writeValueAsString(answer);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

			return answerPayload;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String,Object> removeEmptyMaps(Map map, String movement){
		HashMap<String,Object> mapInput = (HashMap<String, Object>) map;
		HashMap<String,Object> answerResult = new HashMap<>();
		for (String key : mapInput.keySet()){
			//System.out.println(movement+key +" - "+ mapInput.get(key).getClass());
			if (map.get(key).getClass().equals(LinkedHashMap.class)) {
				LinkedHashMap Map = (LinkedHashMap) map.get(key);
				//System.out.println(Map.size());
				if (Map.size()>0) {
					answerResult.put(key, removeEmptyMaps(Map, movement + "-"));
				}
				else
				{
					// Dont want empty maps
				}
			}
			else if (map.get(key).getClass().equals(ArrayList.class))
			{
				//System.out.println(key + " - array");
				answerResult.put(key, checkForEmptyArrayElements((ArrayList<Object>) map.get(key), movement+"-"));
			}
			else
			{
				answerResult.put(key, mapInput.get(key));
			}
		}
		return answerResult;
	}
	
	public ArrayList<Object> checkForEmptyArrayElements(ArrayList<Object> array, String movement){
		ArrayList<Object> secondArray = new ArrayList<>();
		for (Object obj : array) {
			//System.out.println(movement+"["+" - "+obj.getClass());
			
			if (obj.getClass().equals(LinkedHashMap.class)) {
				LinkedHashMap Map = (LinkedHashMap) obj;
				if (Map.size()>0) {
					secondArray.add(removeEmptyMaps(new HashMap<String,Object>(Map), movement+"-"));
				}
				else
				{
					// no touchy
				}
			}
		}
		return secondArray;
	}

}
