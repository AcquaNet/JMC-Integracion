package sapb1masterpoll;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.mule.DefaultMuleEvent;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;

public class WhileLoop implements Callable {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@SuppressWarnings("unchecked")
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		LOG.info("JAVA.WhileLoop.31: ActiveMQLooper OnCall Triggered");
		String ActiveMQMessage = "";

		// Pickup messages until there are no more in MQ Queue
		Set<String> vars = eventContext.getMessage().getInvocationPropertyNames();
		HashMap<String, Object> flowVars = new HashMap<String, Object>();
		for (String str : vars) {
			flowVars.put(str, eventContext.getMessage().getInvocationProperty(str));
		}

		HashMap<String, Object> results = new HashMap<String, Object>();
		results.put("value", new ArrayList<HashMap<String, Object>>());
		ArrayList<HashMap<String, Object>> resultArray = new ArrayList<HashMap<String, Object>>();
		MuleEvent event = null;

		String flowName = (String) flowVars.get("Fetch_flowReference");
		int SkipCounter = 0;
		flowVars.put("Fetch_SkipCounter", SkipCounter);
		event = invokeMuleFlow(message, eventContext.getMuleContext(), flowName, flowVars);
		if (!event.getMessage().getPayload().getClass().equals(HashMap.class)) {
			event.getMessage().setPayload(StringToJSON.jsonToMap((JSONObject) event.getMessage().getPayload()));
		}
		HashMap<String, Object> pyload = (HashMap<String, Object>) event.getMessage().getPayload();
		if (((ArrayList<HashMap<String, Object>>) pyload.get("value")).size() == 0) {
			LOG.info("JAVA.WhileLoop.56: No results");
			return results;

		}
		if (!hasMoreResults(event)) {
			// System.out.println("Has no more results");
			LOG.info("JAVA.WhileLoop.62: hasMoreResults false");
			HashMap<String, Object> getResult = (HashMap<String, Object>) event.getMessage().getPayload();
			ArrayList<HashMap<String, Object>> odataResutls = (ArrayList<HashMap<String, Object>>) getResult
					.get("value");
			for (HashMap<String, Object> result : odataResutls) {
				resultArray.add(result);
			}
		} else {
			while (hasMoreResults(event)) {
				LOG.info("JAVA.WhileLoop.71: hasMoreResults true");
				HashMap<String, Object> getResult = (HashMap<String, Object>) event.getMessage().getPayload();
				ArrayList<HashMap<String, Object>> odataResutls = (ArrayList<HashMap<String, Object>>) getResult
						.get("value");
				int i = resultArray.size();
				for (HashMap<String, Object> result : odataResutls) {
					resultArray.add(result);
				}
				LOG.info("Ammount of results picked up on this loop: " + (resultArray.size() - i));
				// "odata.nextLink":
				// "/b1s/v1/Items?$select=UpdateDate,ItemCode,ItemName&$skip=20"
				event = null;

				if ((String) getResult.get("odata.nextLink") != null) {
					String url = (String) getResult.get("odata.nextLink");

					url = java.net.URLDecoder.decode(url, "UTF-8");
					// System.out.println("URL TO COMPLIE TO:" + url);
					Pattern patt = Pattern.compile(
							"\\/b1s\\/v1\\/" + "U_TPLAN" + "\\?\\$skip=(.+)");
					Matcher match = patt.matcher(url);
					// System.out.println(patt.pattern());
					// System.out.println("status: " + match.find());
					if (match.matches()) {
						SkipCounter = Integer.valueOf(match.group(1));
						flowVars.put("Fetch_SkipCounter", SkipCounter);
						LOG.info("JAVA.WhileLoop.97: Current skip count " + SkipCounter);
						if (SkipCounter < 5000) {
							LOG.info("JAVA.WhileLoop.99: Invoking a new request");
							event = invokeMuleFlow(message, eventContext.getMuleContext(), flowName, flowVars);
						} else {
							LOG.info("JAVA.WhileLoop.101: Count exceeded 5000. Stopping.");
						}
					}
					else {
						LOG.info("JAVA.WhileLoop.105: Matcher failed. "+url + " | "+patt.toString());
					}
				} else {
					LOG.info("JAVA.WhileLoop.110: odata.nextLink not present.");
					String str = StringToJSON.javaToJSONToString(getResult);
					LOG.info("JAVA.WhileLoop.112: Result recived: " + str);
				}
				if (event != null) {
					LOG.info("JAVA.WhileLoop.115: Loop is finished, starting next one.");
				} else {
					LOG.info("JAVA.WhileLoop.115: Loop is finished, next event is null.");
					String str = StringToJSON.javaToJSONToString(getResult);
					// LOG.info("JAVA.WhileLoop.121: Result recived: "+str);
				}
			}
			HashMap<String, Object> getResult = null;
			if (event.getMessage() != null) {
			getResult = (HashMap<String, Object>) event.getMessage().getPayload();
			}
			ArrayList<HashMap<String, Object>> odataResutls = (ArrayList<HashMap<String, Object>>) getResult
					.get("value");
			int i = resultArray.size();
			for (HashMap<String, Object> result : odataResutls) {
				resultArray.add(result);
			}

			LOG.info("Ammount of results picked up on outgoing loop: " + (resultArray.size() - i));
		}
		LOG.info("JAVA.WhileLoop.124: While Loop Finished. Returning " + resultArray.size() + " Results");
		// results.put("value", resultArray);
		eventContext.getMessage().setInvocationProperty("Fetch_resultFromLoop", resultArray);
		return resultArray;

	}

	public static MuleEvent invokeMuleFlow(MuleMessage muleMessage, MuleContext muleContext, String flowName,
			HashMap<String, Object> flowVars) throws Exception {
		LOG.info("JAVA.WhileLoop.124: Flow LoopingFlow Lookup");
		Flow flow = (Flow) muleContext.getRegistry().lookupFlowConstruct(flowName);
		// System.out.println("Flow Found");
		LOG.info("JAVA.WhileLoop.127: Create muleEvent for invoking flow and set flowVars");
		MuleEvent muleEvent = new DefaultMuleEvent(muleMessage, MessageExchangePattern.REQUEST_RESPONSE, flow);
		for (String str : flowVars.keySet()) {
			muleEvent.setFlowVariable(str, flowVars.get(str));
		}
		LOG.info("JAVA.WhileLoop.132: Invoke flow");
		// System.out.println("Flow processing");
		return flow.process(muleEvent);
	}

	@SuppressWarnings("unchecked")
	public static Boolean hasMoreResults(MuleEvent event) throws Exception {
		LOG.info("JAVA.WhileLoop.148: hasMoreResults Invoked");
		if (event != null) {
			if (event.getMessage().getPayload() != null) {
				if (event.getMessage().getPayload().getClass().equals(HashMap.class)) {
					if (((HashMap<String, Object>) event.getMessage().getPayload()).get("odata.nextLink") != null) {
						LOG.info("JAVA.WhileLoop.153: hasMoreResults returns true");
						return true;
					} else {
						LOG.info("JAVA.WhileLoop.156: odata.nextLink is null");
						LOG.info("JAVA.WhileLoop.157: Payload:"+event.getMessage().getPayloadAsString());
					}
				} else {
					LOG.info("JAVA.WhileLoop.162: Class isnt a HashMap");
					// LOG.info("JAVA.WhileLoop.163: Payload:
					// "+event.getMessage().getPayloadAsString());
				}
			} else {
				LOG.info("JAVA.WhileLoop.168: Payload is null");
				// LOG.info("JAVA.WhileLoop.169: Payload:
				// "+event.getMessage().getPayloadAsString());
			}
		} else {
			LOG.info("JAVA.WhileLoop.159: Event is null");
		}

		LOG.info("JAVA.WhileLoop.179: hasMoreResults returns false");
		return false;
	}

	public static Long getLongTimestamp(String date) {
		Pattern patt = Pattern.compile("\\/Date\\((.+)\\-(.+)\\)\\/");
		Matcher match = patt.matcher(date);
		Long timestamp = 0L;
		if (match.matches()) {
			timestamp = Long.valueOf(match.group(1));
		}
		return timestamp;
	}
}
