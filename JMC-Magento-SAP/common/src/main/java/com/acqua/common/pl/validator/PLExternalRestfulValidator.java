/**
 * Mule Service Layer External Restful Validator
 *
 * Acqua IT
 *
 */
package com.acqua.common.pl.validator; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.api.MuleEvent;
import org.mule.extension.validation.api.ValidationResult;
import org.mule.extension.validation.api.Validator;
import org.mule.extension.validation.internal.ImmutableValidationResult; 
import com.acqua.common.pl.model.PLErrorCode;
import com.acqua.common.pl.model.PLResponseError; 
import com.acqua.common.sl.exceptions.SLResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Process Layer REST Validator
 *
 *	Input: 	(String) flowVars.flowName (Optional) Default Blank
 *			(String) flowVars.metricCategory  (Optional) Default Blank
 *			(String) flowVars.apiName (Optional)  
 *		   	(String) flowVars.errorCode (Optional) Default 0000 
 *
 *  Output  (PLResponseError) flowVars.ProcessLayerResponseError
 *	
 * @author Acqua IT
 */
public class PLExternalRestfulValidator implements Validator {
	
	private static Logger logger = LogManager.getLogger(PLExternalRestfulValidator.class.getName());

	@Override
	public ValidationResult validate(MuleEvent event) {

		// ==================================
		// Init Working Vars
		// ==================================

		String flowName = "";
		String metricCategory = "";
		String errorCode = "0000";
		String errorMessage = "";
		int httpStatus = 500;
		
		SLResponseError slResponseError = null;
		
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
		 
  
		} catch (Exception e) {
			  
			errorMessage = e.getMessage();
			
			logger.error(errorMessage);
			
		}
		
		// ==================================
		// Getting Error Message
		// ==================================
		 
		if(errorMessage.isEmpty())
		{
			
			String payload = "";
			
			try {
				
				payload = event.getMessage().getPayloadAsString();
				
				logger.error(payload);
				
				logger.info("Processing Response: " + (String) event.getMessage().getPayloadForLogging());
				
				ObjectMapper om = new ObjectMapper();
				 
				slResponseError = om.readValue((String) payload, SLResponseError.class);
				
			} catch (Exception e1) {
				
				errorMessage = "Error getting Payload: " + e1.getMessage(); 
				
				logger.error(payload,e1);
				
				errorCode = "9001";
				
			}  
			 
			
		}
		 
		// ==================================
		// Create Process Layer Code
		// ==================================
		 
		PLErrorCode processLayerErrorCode = new PLErrorCode();
		
		processLayerErrorCode.setFlowName(flowName);
		processLayerErrorCode.setCategory(metricCategory);
		processLayerErrorCode.setErrorCode(errorCode);
		processLayerErrorCode.setHttpStatus(httpStatus);
		processLayerErrorCode.setErrorMessage(errorMessage); 
		processLayerErrorCode.setSlResponseError(slResponseError);
		
		// ==================================
		// Create Process Response
		// ==================================

		PLResponseError plResponseError = new PLResponseError();
		plResponseError.setErrors(processLayerErrorCode);
		
		// ==================================
		// Create Validator Result
		// ==================================
		
		event.setFlowVariable("ProcessLayerResponseError", plResponseError); 
		
		ValidationResult result = ImmutableValidationResult.error("Error Invocando System Layer");
		  
		return result;
		  
	}

}
