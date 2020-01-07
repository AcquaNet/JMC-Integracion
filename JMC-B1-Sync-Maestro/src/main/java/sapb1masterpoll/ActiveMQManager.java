package sapb1masterpoll;

import java.util.HashMap;

import javax.jms.*;

import org.mule.api.context.notification.MuleContextNotificationListener;
import org.mule.context.notification.MuleContextNotification;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.log4j.Logger;

public class ActiveMQManager implements MuleContextNotificationListener<MuleContextNotification> {
	
	private final String MULE_CONTEXT_EVENT_STARTED = "mule context started";
	private static String[] pathList;
	private static String[] destinations;

	public static String host;

	public static String user;

	public static String pass;
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	@Override
	public void onNotification(MuleContextNotification notification) {
		if (notification.getActionName() == MULE_CONTEXT_EVENT_STARTED) {
			try {
				pathList = System.getProperty("sapB1.pathList").split(",");
				destinations = System.getProperty("sapB1.destinations").split(",");
		          LOG.info("JAVA.ActiveMQManager.32: ################## STARTUP JAVA LOGIC ##################");
		          //generateQueues();
			} catch (Exception e) {
				for (StackTraceElement str : e.getStackTrace()) {
					LOG.error("JAVA.ActiveMQManager.36:"+str.getFileName() + " "+str.getMethodName()+ " " + str.getLineNumber());
				}
				
			}
		}
	}

	public static void setProperty(String prop, String val) {
		LOG.info("JAVA.ActiveMQManager.44: Set Property ** "+prop+":"+val+" **");
		System.setProperty(prop, val);
	}
	
	// Funcion sin uso
	public static void generateQueues() throws JMSException {
		// Create a ConnectionFactory
		String[] str = destinations;
		host = "tcp://localhost:61616";
		user = "admin";
		pass = "admin";
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(host);
		
		// Create a Connection
		Connection connection = connectionFactory.createConnection(user,pass);
		connection.start();

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		
		// Create the destination (Topic or Queue)
		for (String dest : str) {
			for (String path : pathList) {
				LOG.info("JAVA.ActiveMQManager.68: Queue_" + path + "_" + dest);
				//Destination dst = session.createQueue("Queue_" + path + "_" + dest);
				//@SuppressWarnings("unused")
				//MessageProducer prodc = session.createProducer(dst);
			}
		}

		// Clean up
		session.close();
		connection.close();

	}
	
	
	private static ActiveMQConnectionFactory factory;
	private static HashMap<String, Destination> sendDestination = new HashMap<String,Destination>();
	private static HashMap<String,MessageProducer> producer = new HashMap<String,MessageProducer>();
	
	// Inicia una conexion 
	public static Connection connect() throws JMSException {
		host = System.getProperty("host");
		user = System.getProperty("user");
		pass = System.getProperty("pass");
		LOG.info("JAVA.ActiveMQManager.91: Creating Connection to MQ");
		LOG.info("JAVA.ActiveMQManager.92: Host: "+host);
		LOG.info("JAVA.ActiveMQManager.93: user: "+user);
		LOG.info("JAVA.ActiveMQManager.94: pass: "+pass);
		factory = new ActiveMQConnectionFactory(host);
        RedeliveryPolicy policy = factory.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(-1);
        Connection connection =  factory.createConnection(user, pass);
        LOG.info("JAVA.ActiveMQManager.99: Starting connecton to MQ");
        connection.start();	
        LOG.info("JAVA.ActiveMQManager.101: Connection to MQ Started");
        return connection;

	}
	
	// Mata la conexion
	public static void endConnection(Connection conn) throws JMSException {
		conn.close();
	}
	
	// Crea una sesion
	public static Session CreateSession(String queue, Connection conn) throws JMSException {
		LOG.info("JAVA.ActiveMQManager.113: Creating a new Session");
		return conn.createSession(true, Session.CLIENT_ACKNOWLEDGE);


	}
	
	public static Destination createDestination(String queue, Session session) throws JMSException {
        LOG.info("JAVA.ActiveMQManager.120: Creating a message reader Queue: "+queue);
        return session.createQueue(queue);
	}
	
	public static MessageConsumer createConsumer(String queue, Session session, Destination dest) throws JMSException{
        LOG.info("JAVA.ActiveMQManager.125: Creating a consumer for Destination "+queue);
        return session.createConsumer(dest);
	}
	
	
	public static void closeConsumer(MessageConsumer consumer) throws JMSException{
		LOG.info("JAVA.ActiveMQManager.131: Closing session and consumer");
		consumer.close();
	}
	
	public static void closeSession(Session session) throws JMSException{
		session.close();
	}
	
	// Crea una session de enviar mensajes
	public static Session CreateProducerSession(Connection conn) throws JMSException {
		LOG.info("JAVA.ActiveMQManager.141: Create a Producer Session");
		return conn.createSession(true, Session.CLIENT_ACKNOWLEDGE);
	}
	
	// Mata la session
	public static void closeProducerSession(Session session) throws JMSException{
		LOG.info("JAVA.ActiveMQManager.147: End Producer Session");
		session.close();
	}

	// Crea un endpoint de mensajes para dicha Queue 
	public static void createMessageProducer(String queue, Session session) throws JMSException {
		LOG.info("JAVA.ActiveMQManager.153: create a message producer for Queue: "+queue);
        sendDestination.put(queue, session.createQueue(queue));
        producer.put(queue,session.createProducer(sendDestination.get(queue)));
	}
	
	
	// Mata el endpoint de mensajes para dicha Queue
	public static void endMessageProducer(String queue) throws JMSException {
		LOG.info("JAVA.ActiveMQManager.161: End message producer for queue: "+queue); 
		sendDestination.remove(queue);
		if (producer.get(queue) != null) {
		producer.get(queue).close();
		}
	}
	
	public static void commitTransaction(Session session) throws JMSException {
		LOG.info("JAVA.ActiveMQManager.169: Session Commit");	
		session.commit();
	}
	
	public static String getMessageFromQueue(String queue, MessageConsumer consumer) throws JMSException {
		String message = null;
        
        TextMessage reply = (TextMessage)consumer.receive(1000);
        if (reply != null)
        message = reply.getText();
        LOG.info("JAVA.ActiveMQManager.179: Mensaje levantado: "+message);
		return message;
	}

	public static void sendMessageToQueue(String queue, Session session, String message) throws JMSException {
		TextMessage msg = session.createTextMessage();
		msg.setText(message);
		LOG.info("Mensaje para enviar: "+message);
		LOG.info("Enviar mensaje a queue: "+queue);
		producer.get(queue).send(msg);
	}
}
