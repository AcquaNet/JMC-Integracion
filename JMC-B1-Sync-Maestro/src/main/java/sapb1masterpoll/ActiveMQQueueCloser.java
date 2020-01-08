package sapb1masterpoll;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class ActiveMQQueueCloser {
	public static String host;
	public static String user;
	public static String pass;	
	private static ActiveMQConnectionFactory factory;

	public Boolean destroyQueue(String host, String user, String pass, String queue) throws JMSException {
		factory = new ActiveMQConnectionFactory(host);
		ActiveMQConnection connection =  (ActiveMQConnection) factory.createConnection(user, pass);
		if (connection != null) {
			connection.destroyDestination(new ActiveMQQueue(queue));
		}
		else
		{
			return false;
		}
		connection.close();
		return true;
	}
}
