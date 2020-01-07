/**
 * Mule Service Layer Common Validator
 *
 * Acqua IT
 *
 */
package com.acqua.common.pl.validator;

import org.mule.api.MuleEvent; 
import org.mule.extension.validation.api.ValidationResult;
import org.mule.extension.validation.api.Validator;
import org.mule.extension.validation.internal.ImmutableValidationResult;

import com.acqua.common.pl.model.PLErrorCode;
import com.acqua.common.pl.model.PLResponseError; 
  
/**
 * Process Layer Common Validator
 *
 *	Input: 	(String) flowVars.flowName (Optional) Default Blank
 *			(String) flowVars.metricCategory  (Optional) Default Blank
 *			(String) flowVars.errorMessage (Mandatory) / Message error
 *		   	(String) flowVars.errorCode (Optional) Default 0000
 *		   	(Integer) flowVars.httpStatus (Optional) Http Status to return / Default 500
 *
 *  Output  (PLResponseError) flowVars.ProcessLayerResponseError
 *	
 * @author Acqua IT
 */
public class PLCommonValidator implements Validator {

	@Override
	public ValidationResult validate(MuleEvent event) {
		
		
		// --------------------------------------
		// Validate Parameters
		// --------------------------------------
		
		String flowName = "";
		String metricCategory = "";
		String errorCode = "0000";
		Integer httpStatus = 500;
		String errorMessage = "";
		
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

			if (event.getFlowVariableNames().contains("httpStatus")) {
				httpStatus = (Integer) event.getFlowVariable("httpStatus");
			}

			// --------------------------------------
			// Get Error Message from the Flow
			// --------------------------------------

			errorMessage = event.getFlowVariable("errorMessage");

		} catch (Exception e) {
			  
			errorMessage = e.getMessage();
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
 
		// Create ImmutableValidationResult

		ValidationResult result = ImmutableValidationResult.error(errorMessage);

		// ==================================
		// Create Process Response
		// ==================================

		PLResponseError plResponseError = new PLResponseError();
		plResponseError.setErrors(processLayerErrorCode);

		event.setFlowVariable("ProcessLayerResponseError", plResponseError); 

		return result;
	}

}
