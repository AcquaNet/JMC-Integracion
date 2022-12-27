package acqua.util.Transform;

import java.io.File;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;


public class BorrarRecuentoEntradaMercancia extends AbstractMessageTransformer {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("jmc_hh.log");

	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Get information for file
		String sociedad = (String) message.getInvocationProperty("sociedad");
		String operacion = (String) message.getInvocationProperty("codigo");

		// Check if main directory exists, otherwise create
		File directorioPrincipal = new File("entradasMercancia/");
		if (!directorioPrincipal.exists()) {
			directorioPrincipal.mkdir();
		}

		// Check if society directory exists, otherwise create
		File directorioSociedad = new File("entradasMercancia/" + sociedad + "/");
		if (!directorioSociedad.exists()) {
			directorioSociedad.mkdir();
		}

		// Check if file exists
		String fileName = "entradasMercancia/" + sociedad + "/" + operacion + ".json";
		File file = new File(fileName);
		if (!file.exists()) {
			// If it doesnt...
				LOG.info("No existe el archivo "+fileName);
				return false;
		} else {
			file.delete(); // No queremos borrarlo al instante - Si queremos
			return true;

		}
	}
	
}
