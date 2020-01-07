package com.acqua.magento;

import javax.xml.rpc.ServiceException;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.springframework.beans.factory.annotation.Value;

import com.acqua.common.sl.exceptions.SLExternalErrorCode;
import com.acqua.magento.ServerPortType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MagentoCallable implements Callable {
	
		ServerPortType magento;
	
		@Value("${magento_url}")
		private String url;
		
		
		public Object onCall(MuleEventContext muleContext) throws Exception {
			String response = "";
			
			ObjectMapper mapper = new ObjectMapper();
			
			MuleMessage message = muleContext.getMessage();
			
			SLExternalErrorCode externalErrorCode = null;
			
			
			String input = (String) message.getPayload();
			Articulo articulos[] = mapper.readValue(input, Articulo[].class);
			
			prepareConnection();
			
			try {
				
			response = magento.gestionarStock(articulos);
			
			} catch (Exception e){
				externalErrorCode = new SLExternalErrorCode();

				externalErrorCode.setHttpStatus(500); 
				externalErrorCode.setApiName(message.getInvocationProperty("apiName"));
				externalErrorCode.setErrorMessage(e.getMessage());
				externalErrorCode.setResult("");
				externalErrorCode.setResult_namespace("");
			}
			if (response.contains("errors")) {
				externalErrorCode = new SLExternalErrorCode();

				externalErrorCode.setHttpStatus(500); 
				externalErrorCode.setApiName(message.getInvocationProperty("apiName"));
				externalErrorCode.setErrorMessage("An error was found on api response: " + response);
				externalErrorCode.setResult("");
				externalErrorCode.setResult_namespace("");
			}
			if(externalErrorCode!=null)
			{
				message.setProperty("ExternalJavaErrorCode", externalErrorCode, PropertyScope.INVOCATION);
			} 
			
			return response;
		}
		
		private void prepareConnection() throws ServiceException {
			ServerLocator locator = new ServerLocator();
			
			if (url != null ? (!url.equals("")) : false){
				locator.setEndpointAddress("serverPort", url);
			}
			// Recuperar el servicio
			magento = locator.getserverPort();

		}
}
