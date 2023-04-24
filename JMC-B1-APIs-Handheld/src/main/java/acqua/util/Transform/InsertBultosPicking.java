package acqua.util.Transform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;


public class InsertBultosPicking extends AbstractMessageTransformer implements MuleContextAware {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");
	
	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		/* Se recupera informacion */
		HashMap<String, Object> listaBultos = (HashMap<String, Object>) message.getPayload();
		String sociedad = (String) message.getInvocationProperty("sociedad");
		String entorno = (String) message.getInvocationProperty("entorno");

		return null;
	}

	
}
