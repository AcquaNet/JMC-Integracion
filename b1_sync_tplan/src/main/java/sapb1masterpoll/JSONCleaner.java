package sapb1masterpoll;


import java.util.HashMap;
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
			String answerPayload = null;
	        try {
	            answerPayload = mapper.writeValueAsString(answer);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

			return answerPayload;
	}
	

}
