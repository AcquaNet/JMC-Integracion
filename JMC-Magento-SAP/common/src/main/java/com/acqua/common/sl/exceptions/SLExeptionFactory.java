/**
 * Mule Service Layer Common Create Reposne
 *
 * Acqua IT
 *
 */
package com.acqua.common.sl.exceptions;
 
import org.mule.api.MuleEvent;
import org.mule.extension.validation.api.ExceptionFactory;
import org.mule.extension.validation.api.ValidationResult;
   
public class SLExeptionFactory implements ExceptionFactory {
 

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Exception> T createException(ValidationResult result, Class<T> exceptionClass, MuleEvent event) {
 
		SLResponseError systemLayerResponseError = event.getFlowVariable("SystemLayerResponseError");
		 
		SLException exception = new SLException(result, event);
 
		exception.setSystemLayerResponseError(systemLayerResponseError);

		return (T) exception;
	}

	@Override
	public Exception createException(ValidationResult result, String exceptionClassName, MuleEvent event) {

		 
		SLResponseError systemLayerResponseError = event.getFlowVariable("SystemLayerResponseError");
		 
		SLException exception = new SLException(result, event);
 
		exception.setSystemLayerResponseError(systemLayerResponseError);

		return exception;
	}

}
