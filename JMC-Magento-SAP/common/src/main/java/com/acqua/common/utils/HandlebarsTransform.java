package com.acqua.common.utils;

import java.util.HashMap;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlebarsTransform implements Callable {
	
	private Logger logger = LoggerFactory.getLogger(CommonLayer.class); 

	private Template template;

    @Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		HashMap<String, Object> payload = new HashMap<String, Object>();
		TemplateLoader loader = new ClassPathTemplateLoader();
		
		loader.setPrefix("/templates");
		loader.setSuffix(".hbs");

		Handlebars handlebars = new Handlebars(loader);
		try {
			template = handlebars.compile("layer-fail-report");
			MuleMessage message = eventContext.getMessage();
			payload.put("payload", message.getPayload());

		} catch (Exception e) {
			logger.info("Failed to transform template: " + e.toString());
		}

        return template.apply(payload);
    }

	
    

}
