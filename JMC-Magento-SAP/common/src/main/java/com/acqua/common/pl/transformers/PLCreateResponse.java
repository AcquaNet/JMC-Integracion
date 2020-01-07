package com.acqua.common.pl.transformers;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException; 
import org.mule.transformer.AbstractMessageTransformer; 
import org.mule.transformer.types.DataTypeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acqua.common.pl.model.PLErrorCode;
import com.acqua.common.pl.model.PLResponseError;
import com.acqua.common.sl.exceptions.SLResponseError;

/**
 * Process Layer Create Response
 *
 *	Input: 	(String) flowVars.flowName (Optional) Default Blank
 *			(String) flowVars.metricCategory  (Optional) Default Blank
 *			(String) flowVars.errorCode (Optional) Default  
 *		   	(Integer) flowVars.httpStatus (Optional) Http Status to return / Default 500
 *
 *  Output  (SLResponseError) flowVars.ProcessLayerResponseError
 *	
 * @author Acqua IT
 */
public class PLCreateResponse extends AbstractMessageTransformer{
	
	private Logger logger = LoggerFactory.getLogger(PLCreateResponse.class);
  
	public PLCreateResponse()
    {
        super();
        
        this.registerSourceType(DataTypeFactory.create(Object.class));
     
        this.setReturnDataType(DataTypeFactory.create(SLResponseError.class));
        
    }
 
	@Override
	public PLResponseError transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// --------------------------------------
		// Validate Parameters
		// --------------------------------------
		
		String flowName = "";
		String metricCategory = "";
		String errorCode = "";
		int httpStatus = 500;
		String errorMessage = "";
		
		try {
			
			
			if (message.getInvocationPropertyNames().contains("flowName")) {
				flowName = message.getInvocationProperty("flowName");
			}
			
			if (message.getInvocationPropertyNames().contains("metricCategory")) {
				metricCategory = message.getInvocationProperty("metricCategory");
			}
			
			if (message.getInvocationPropertyNames().contains("errorCode")) {
				errorCode = message.getInvocationProperty("errorCode");
			}
			
			if (message.getInvocationPropertyNames().contains("httpStatus")) {
				httpStatus = message.getInvocationProperty("httpStatus");
			}
			 

		} catch (Exception e) {
			  
			errorMessage = e.getMessage();
		}
		
		// ===================================
		// Getting Error Message
		// ===================================

		if (errorMessage.isEmpty()) {

			ExceptionPayload errorMessagePayload = message.getExceptionPayload();

			logger.info("Error Message: " + errorMessagePayload.getMessage());
			logger.info("Error Exception Name: " + errorMessagePayload.getException().getClass().getName());
			logger.info("Error Exception Message: " + errorMessagePayload.getException().getMessage());

			if (errorMessagePayload.getException().getCause() != null) {
				logger.info(
						"Error Exception Cause Message: " + errorMessagePayload.getException().getCause().getMessage());
				logger.info("Error Exception Cause Name: "
						+ errorMessagePayload.getException().getCause().getClass().getName());
				logger.info("Error Exception Cause Name: "
						+ errorMessagePayload.getException().getCause().getStackTrace().toString());
			}
			
			errorMessage = errorMessagePayload.getMessage();

		}
		 
		// ===================================
		// PROCESS LAYER ERROR CODE
		// ===================================
		 
		PLErrorCode slErrorCode = new PLErrorCode();
		slErrorCode.setFlowName(flowName);
		slErrorCode.setCategory(metricCategory);
		slErrorCode.setErrorCode(errorCode);
		slErrorCode.setErrorMessage(errorMessage);
		slErrorCode.setSlResponseError(null);
		slErrorCode.setHttpStatus(httpStatus);
		
		// ===================================
		// SYSTEM LAYER RESPONSE OBJECT
		// ===================================
		  
		PLResponseError plResponse = new PLResponseError();
		plResponse.setErrors(slErrorCode);
		
		return plResponse;
	}
  
}
