package com.acme;

import java.util.Map;
import javax.jms.ConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Message;
import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;    
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.language.JsonPathExpression;
import org.apache.camel.model.dataformat.JsonLibrary;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import java.io.InputStreamReader;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;

/**
 * A Camel Java DSL Router
 */
public class DASBankRouteBuilder extends RouteBuilder {
    private final ConnectionFactory connectionFactory;
    
    public DASBankRouteBuilder(ConnectionFactory connectionFactory){
	this.connectionFactory = connectionFactory;
    }
    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() throws IOException, ScriptException, Exception {	
	// Note we can explicit name the component
	if(getContext().getComponent("jms", false) == null){
	    getContext().addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	}
	
        from("jms:queue:das.failed")
	    .log("FAILED: ${in.header.operation}:${in.body}")
	    .convertBodyTo(String.class)
	    .to("mongodb:mongoBean?database=ibank&collection=failed&operation=insert");
	

        from("jms:queue:das.completed")
	    .log("COMPLETED: ${in.header.operation}:${in.body}")
	    .convertBodyTo(String.class)
	    .to("mongodb:mongoBean?database=ibank&collection=completed&operation=insert");
	    
    }

}
