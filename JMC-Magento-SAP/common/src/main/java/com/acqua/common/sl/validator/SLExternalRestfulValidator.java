/**
 * Mule Service Layer Common Validator
 *
 * Acqua IT
 *
 */
package com.acqua.common.sl.validator;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.api.MuleEvent;
import org.mule.extension.validation.api.ValidationResult;
import org.mule.extension.validation.api.Validator;
import org.mule.extension.validation.internal.ImmutableValidationResult;

import com.acqua.common.sl.exceptions.SLErrorCode;
import com.acqua.common.sl.exceptions.SLExternalErrorCode;
import com.acqua.common.sl.exceptions.SLResponseError;

/**
 * Service Layer REST Validator
 *
 *	Input: 	(String) flowVars.flowName (Optional) Default Blank
 *			(String) flowVars.metricCategory  (Optional) Default Blank
 *			(String) flowVars.apiName (Optional)  
 *		   	(String) flowVars.errorCode (Optional) Default 0000 
 *
 *  Output  (SLResponseError) flowVars.SystemLayerResponseError
 *	
 * @author Acqua IT
 */
public class SLExternalRestfulValidator implements Validator {
	
	private static Logger logger = LogManager.getLogger(SLExternalRestfulValidator.class.getName());

	@Override
	public ValidationResult validate(MuleEvent event) {

		// ==================================
		// Init Working Vars
		// ==================================

		String flowName = "";
		String metricCategory = "";
		String apiName = "";
		String errorCode = "0000";
		String errorMessage = "";
		int httpStatus = 0;
		
		// ==================================
		// Validate External Service Call
		// ==================================
		 
				
		if(event.getMessage().getInboundPropertyNames().contains("http.status"))
		{ 
			httpStatus = event.getMessage().getInboundProperty("http.status");
			
			if(httpStatus==200 || httpStatus==201)
			{ 
				ValidationResult result = ImmutableValidationResult.ok();

				return result;
				 
			}
			
		}
		else
		{
			httpStatus = 400;
			errorMessage = "Cannot validate response";
			errorCode = "9000";
		}
		 
		// --------------------------------------
		// Validate Parameters
		// --------------------------------------

		try {
			 
			if (event.getFlowVariableNames().contains("flowName")) {
				flowName = (String) event.getFlowVariable("flowName");
			}
 
			if (event.getFlowVariableNames().contains("metricCategory")) {
				metricCategory = (String) event.getFlowVariable("metricCategory");
			}
 
			if (event.getFlowVariableNames().contains("errorCode")) {
				errorCode = (String) event.getFlowVariable("errorCode");
			} 
			
			if (event.getFlowVariableNames().contains("apiName")) {
				apiName = (String) event.getFlowVariable("apiName");
			} 
  
		} catch (Exception e) {
			  
			errorMessage = e.getMessage();
			
			logger.error(errorMessage);
			
		}
		
		// ==================================
		// Getting Error Message
		// ==================================
		
		if(errorMessage.isEmpty())
		{
			
			try {
				
				errorMessage = event.getMessage().getPayloadAsString();
				
				logger.error(errorMessage);
				
			} catch (Exception e1) {
				
				errorMessage = "Error getting Payload: " + e1.getMessage(); 
				
				logger.error(errorMessage,e1);
				
			} 
			 
			
		}
		  
		// ==================================
		// Create ExternalErrorCode
		// ==================================
		
		 SLExternalErrorCode externalErrorCode = new SLExternalErrorCode();
		 
		 externalErrorCode.setHttpStatus(httpStatus);
		 externalErrorCode.setApiName(apiName);
		 externalErrorCode.setErrorMessage("Error ejecutando API " + event.getFlowVariable("categoryName"));
		 externalErrorCode.setResult(errorMessage);
		 externalErrorCode.setResult_namespace("");
		
		// ==================================
		// Create System Layer Code
		// ==================================
		 
		 SLErrorCode systemLayerErrorCode = new SLErrorCode();
		
		systemLayerErrorCode.setFlowName(flowName);
		systemLayerErrorCode.setCategory(metricCategory);
		systemLayerErrorCode.setErrorCode(errorCode);
		systemLayerErrorCode.setHttpStatus(httpStatus);
		systemLayerErrorCode.setErrorMessage(errorMessage); 
		systemLayerErrorCode.setExternalErrorCode(externalErrorCode);
		
		// ==================================
		// Create System Response
		// ==================================

		SLResponseError response = new SLResponseError();
		response.setErrors(systemLayerErrorCode);
		
		// ==================================
		// Create Validator Result
		// ==================================
		
		event.setFlowVariable("SystemLayerResponseError", response); 
		
		ValidationResult result = ImmutableValidationResult.error("Error Invocando Servicio Externo");
		  
		return result;
		  
	}

}
