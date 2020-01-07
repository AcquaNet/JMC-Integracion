package com.modus.funtional;

import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.MuleMessageCollection; 
import org.mule.tck.junit4.FunctionalTestCase;

public class BiostartTestCases extends FunctionalTestCase {

	@Override
	protected String[] getConfigFiles() {
		return new String[] { "src/main/app/global.xml", "src/main/app/error-handler.xml",
				"src/main/app/sys-global-exception.xml", "src/main/app/biostar2-system.xml" };
	}

	@Test
	public void testFlow() throws Exception {

		String inputPayload = "{\"subdomain\": \"impresiones\",\"pass\": \"oficina13\",\"user\": \"srepetto\"}";

	}

}
