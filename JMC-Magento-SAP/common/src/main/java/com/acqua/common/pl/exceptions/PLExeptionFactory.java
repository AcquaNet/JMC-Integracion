/**
 * Mule Service Layer Common Create Reposne
 *
 * Acqua IT
 *
 */
package com.acqua.common.pl.exceptions;
 
import org.mule.api.MuleEvent;
import org.mule.extension.validation.api.ExceptionFactory;
import org.mule.extension.validation.api.ValidationResult;

import com.acqua.common.pl.model.PLResponseError;
   
public class PLExeptionFactory implements ExceptionFactory {
 

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Exception> T createException(ValidationResult result, Class<T> exceptionClass, MuleEvent event) {
 
		PLResponseError processLayerResponseError = event.getFlowVariable("processLayerResponseError");
		 
		PLException exception = new PLException(result, event);
 
		exception.setProcessLayerResponseError(processLayerResponseError);

		return (T) exception;
	}

	@Override
	public Exception createException(ValidationResult result, String exceptionClassName, MuleEvent event) {

		 
		PLResponseError processLayerResponseError = event.getFlowVariable("processLayerResponseError");
		 
		PLException exception = new PLException(result, event);
 
		exception.setProcessLayerResponseError(processLayerResponseError);

		return exception;
	}

}
