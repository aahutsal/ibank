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

import java.math.BigDecimal;

import java.io.InputStreamReader;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;


/**
 * A Camel Java DSL Router
 */
public class IBankRouteBuilder extends RouteBuilder {
    
    private static final ScriptEngineManager manager = new ScriptEngineManager();
    private static final ScriptEngine engine = manager.getEngineByName("JavaScript");
    private static final AtomicReference<Map> accountsRef = new AtomicReference<Map>(null);

    private final ConnectionFactory connectionFactory;
    
    public IBankRouteBuilder(ConnectionFactory connectionFactory){
	this.connectionFactory = connectionFactory;
    }
    
    
    protected void initializeEngine(CamelContext ctx) 
	throws IOException, ScriptException, Exception {
	ProducerTemplate producer = new DefaultProducerTemplate(ctx);
	producer.start();
	engine.put("producer", producer);
	engine.put("accountsRef", accountsRef);
	engine.eval(new InputStreamReader(new ClassPathResource("js/bank.js").getInputStream()));
    }

    protected Double getDoubleValue(Object obj){
	if(obj instanceof BigDecimal){
	    return ((BigDecimal)obj).doubleValue();
	} else if(obj instanceof Double){
	    return (Double)obj;
	} else if(obj instanceof Integer){
	    return ((Number)obj).doubleValue();
	} else {
	    throw new java.lang.ClassCastException("Class " + obj.getClass().getName() + " can't be casted to Double");
	}
	    
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() throws IOException, ScriptException, Exception {
	initializeEngine(getContext());
	
	// Note we can explicit name the component
	if(getContext().getComponent("jms", false) == null){	    
	    getContext().addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	}

	onException(java.lang.IllegalStateException.class)
	    .handled(true)
	    .logExhausted(false)
	    .logStackTrace(false)
	    .to("file:out/bank/failed?fileName=${date:now:yyyyMMdd-hhmmssSSSS}.${in.header.operation}")
	    .to("jms:queue:das.failed");

	interceptSendToEndpoint("jms:queue:bank.operate")
	    .to("jms:queue:das.completed");
	
	interceptFrom("seda:bank.*")
	    .marshal().json(JsonLibrary.Jackson)
	    .choice()
	    .when().jsonpath("$..from[?(@.account.number != $.to.account.number)]")
	    // Trying to perform transaction. Throwing exception if not possible
	    .process(new Processor(){
		    public void process(Exchange outExchange) {
			final Message message = outExchange.getIn();
			final String operation = message.getHeader("operation").toString();
			final Map accountFrom = (Map)accountsRef.get().get(new JsonPathExpression("$.from.customer").evaluate(outExchange).toString());
			final Map accountTo = (Map)accountsRef.get().get(new JsonPathExpression("$.to.customer").evaluate(outExchange).toString());
			final Double amount = getDoubleValue(new JsonPathExpression("$.amount").evaluate(outExchange));
			
			switch(operation.toLowerCase()){
			case "send": {
			    // Operation should be atomic
			    synchronized(this){
				if(getDoubleValue(accountFrom.get("balance")) < amount){
				    throw new java.lang.IllegalStateException("Insufficient funds");
				} else {
				    accountFrom.put("balance", getDoubleValue(accountFrom.get("balance")) - amount);
				    accountTo.put("balance", getDoubleValue(accountTo.get("balance")) + amount);
				}
			    }
			};
			case "receive": {
			    // Operation should be atomic			    
			    synchronized(this){
				if(getDoubleValue(accountTo.get("balance")) < getDoubleValue(new JsonPathExpression("$.amount").evaluate(outExchange))){
				    throw new java.lang.IllegalStateException("Insufficient funds");
				} else {				
				    accountFrom.put("balance", getDoubleValue(accountFrom.get("balance")) + amount);
				    accountTo.put("balance", getDoubleValue(accountTo.get("balance")) - amount);
				}
			    }
			};
			case "withdraw": {
			    if(getDoubleValue(accountFrom.get("balance")) < getDoubleValue(new JsonPathExpression("$.amount").evaluate(outExchange))){
				throw new java.lang.IllegalStateException("Insufficient funds");
			    } else {
				accountFrom.put("balance", getDoubleValue(accountFrom.get("balance")) - amount);
			    }
			}
			}
		    }
		})
	    .to("jms:queue:bank.operate")
	    .otherwise()	    
	    .filter(header("operation").isNotEqualTo("withdraw"))
  	    // Checking if from and to is the same customer
	    .process(new Processor(){
		    public void process(Exchange outExchange) throws Exception{
			throw new java.lang.IllegalStateException("Can't transfer within same account!");
		    }
		})
	    .end();
	    
        from("seda:bank.send")
	    .log("${in.header.operation}:${in.body}");

        from("seda:bank.receive")
	    .log("${in.header.operation}:${in.body}");

        from("seda:bank.withdraw")
	    .log("${in.header.operation}:${in.body}");

    }

}
