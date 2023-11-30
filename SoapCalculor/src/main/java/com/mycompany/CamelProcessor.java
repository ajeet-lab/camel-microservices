package com.mycompany;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.tempuri.Add;
import org.tempuri.AddResponse;

public class CamelProcessor implements Processor {
	
	private Logger LOGGER = Logger.getLogger("CamelProcessor "+CamelProcessor.class);

    public void process(Exchange exchange) throws Exception { 
    	int val1 = (int) exchange.getIn().getHeader("val1");
    	int val2 = (int) exchange.getIn().getHeader("val2");
    	
    	Add add = new Add();        
        //Get input from exchange
        LOGGER.info("CamelProcessor001 ===> " + exchange.getIn().getBody());
        add.setIntA(val1);
        add.setIntB(val2);
        exchange.getIn().setBody(add);
    }

	

}
