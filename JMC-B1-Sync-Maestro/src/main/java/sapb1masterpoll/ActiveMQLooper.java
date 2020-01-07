package sapb1masterpoll;

import java.util.HashMap;
import java.util.Set;

import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.log4j.Logger;
import org.mule.DefaultMuleEvent;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;

public class ActiveMQLooper implements Callable {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage message = eventContext.getMessage();
		Set<String> vars = eventContext.getMessage().getInvocationPropertyNames();
		HashMap<String, Object> flowVars = new HashMap<String, Object>();
		for (String str : vars) {
			flowVars.put(str, eventContext.getMessage().getInvocationProperty(str));
		}
		LOG.info("JAVA.ActiveMQLooper.23: ActiveMQLooper OnCall Triggered");
		String ActiveMQMessage = sapb1masterpoll.ActiveMQManager
				.getMessageFromQueue(message.getProperty("QueueName", PropertyScope.INVOCATION), (MessageConsumer) flowVars.get("ActiveMQConsumer"));
		// Pickup messages until there are no more in MQ Queue
		while (ActiveMQMessage != null) {
			LOG.info("JAVA.ActiveMQLooper.28: Message picked up from ActiveMQ");
			// System.out.println("Message Processed: "+ActiveMQMessage);
			message.setPayload(ActiveMQMessage);

			// invoke the Logic flow for distributings
			// System.out.println("Invoking Flow");
			LOG.info("JAVA.ActiveMQLooper.34: Set all variables from mule flow to flow invocation");
			vars = eventContext.getMessage().getInvocationPropertyNames();
			flowVars = new HashMap<String, Object>();
			for (String str : vars) {
				flowVars.put(str, eventContext.getMessage().getInvocationProperty(str));
			}
			LOG.info("JAVA.ActiveMQLooper.40: Invoke Mule flow LoopingFlow");
			MuleEvent event = null;
			
			event = invokeMuleFlow(message, eventContext.getMuleContext(), "LoopingFlow", flowVars);
				//System.out.println("### Logging flowVars");
				
				if (event.getMessage().getInboundProperty("http.status").equals(400)) {
					//System.out.println("Issue is BAD REQUEST, Message error: ");
					//System.out.println("URL: "+event.getFlowVariable("RESTType")+ " "+ event.getFlowVariable("requestURL"));
					//System.out.println(event.getMessage().getPayloadAsString());
					Set<String> ErrorVars = event.getFlowVariableNames();
					HashMap<String, Object> ErrorFlowVars = new HashMap<String, Object>();
					for (String str : ErrorVars) {
						ErrorFlowVars.put(str, event.getFlowVariable(str));
					}
					invokeMuleFlow(event.getMessage(), event.getMuleContext(), "B1_Sync_EmailReportingFlow", ErrorFlowVars);
					
				}
			
			// System.out.println("FlowStatus
			// "+event.getFlowVariable("FlowStatus").equals("OK"));
			if (!event.getFlowVariable("FlowStatus").equals("OK")) {
				LOG.info("JAVA.ActiveMQLooper.62: Mule flow invocation did not finish correctly");
				return "NOT OK";
			}
			// Finish transaction for that single message and pickup the next one.}
			// System.out.println("Commiting Transaction");
			LOG.info("JAVA.ActiveMQLooper.67: Commiting Transaction of message");
			sapb1masterpoll.ActiveMQManager.commitTransaction((Session) flowVars.get("ActiveMQSession"));
			LOG.info("JAVA.ActiveMQLooper.69: Pick up next message");
			ActiveMQMessage = sapb1masterpoll.ActiveMQManager
					.getMessageFromQueue(message.getProperty("queue", PropertyScope.INVOCATION),(MessageConsumer) flowVars.get("ActiveMQConsumer"));
		}

		LOG.info("JAVA.ActiveMQLooper.74: No more messages left to distribute on queue");

		return "ALL OK";
	}

	public static MuleEvent invokeMuleFlow(MuleMessage muleMessage, MuleContext muleContext, String flowName,
			HashMap<String, Object> flowVars) throws Exception {
		LOG.info("JAVA.ActiveMQLooper.81: Flow LoopingFlow Lookup");
		Flow flow = (Flow) muleContext.getRegistry().lookupFlowConstruct(flowName);
		// System.out.println("Flow Found");
		LOG.info("JAVA.ActiveMQLooper.84: Create muleEvent for invoking flow and set flowVars");
		MuleEvent muleEvent = new DefaultMuleEvent(muleMessage, MessageExchangePattern.REQUEST_RESPONSE, flow);
		for (String str : flowVars.keySet()) {
			muleEvent.setFlowVariable(str, flowVars.get(str));
		}
		LOG.info("JAVA.ActiveMQLooper.89: Invoke flow");
		// System.out.println("Flow processing");
		return flow.process(muleEvent);
	}
}
