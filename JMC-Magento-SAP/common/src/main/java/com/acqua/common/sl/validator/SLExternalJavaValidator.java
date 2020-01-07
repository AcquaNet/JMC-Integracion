/**
 * Mule Service Layer Common Validator
 *
 * Acqua IT
 *
 */
package com.acqua.common.sl.validator;

import org.mule.api.MuleEvent; 
import org.mule.extension.validation.api.ValidationResult;
import org.mule.extension.validation.api.Validator;
import org.mule.extension.validation.internal.ImmutableValidationResult;

import com.acqua.common.sl.exceptions.SLErrorCode;
import com.acqua.common.sl.exceptions.SLExternalErrorCode;
import com.acqua.common.sl.exceptions.SLResponseError;
  
/**
 * Service Layer Common Validator
 *
 *	Input: 	(String) flowVars.flowName (Optional) Default Blank
 *			(String) flowVars.metricCategory  (Optional) Default Blank
 *			(String) flowVars.errorMessage (Mandatory) / Message error
 *		   	(String) flowVars.errorCode (Optional) Default 0000
 *		   	(Integer) flowVars.httpStatus (Optional) Http Status to return / Default 500
 *
 *  Output  (SLResponseError) flowVars.SystemLayerResponseError
 *	
 * @author Acqua IT
 */
public class SLExternalJavaValidator implements Validator {

	@Override
	public ValidationResult validate(MuleEvent event) {
		
		
		// --------------------------------------
		// Validate Parameters
		// --------------------------------------
		
		String flowName = "";
		String metricCategory = "";
		String errorCode = "0000"; 
		String errorMessage = "";
		
		// --------------------------------------
		// Create Errors Objects
		// --------------------------------------

		SLExternalErrorCode externalErrorCode = null;

		if (event.getFlowVariableNames().contains("ExternalJavaErrorCode")) {

			externalErrorCode = (SLExternalErrorCode) event.getFlowVariable("ExternalJavaErrorCode");
			 
			
		} else {
			
			ValidationResult result = ImmutableValidationResult.ok();

			return result;

		}
				 
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
		}
 

		SLErrorCode serviceLayerErrorCode = null;
		
		serviceLayerErrorCode = new SLErrorCode();
		
		serviceLayerErrorCode.setFlowName(flowName);
		serviceLayerErrorCode.setCategory(metricCategory);
		serviceLayerErrorCode.setErrorCode(errorCode);
		serviceLayerErrorCode.setHttpStatus(externalErrorCode.getHttpStatus());
		serviceLayerErrorCode.setErrorMessage(errorMessage);
		serviceLayerErrorCode.setExternalErrorCode(externalErrorCode);
 
		// Create ImmutableValidationResult

		ValidationResult result = ImmutableValidationResult.error(errorMessage);

		// Create Process Layer ResponseError

		SLResponseError processLayerResponseError = new SLResponseError();
		
		processLayerResponseError.setErrors(serviceLayerErrorCode);

		event.setFlowVariable("SystemLayerResponseError", processLayerResponseError);

		return result;
	}

}
