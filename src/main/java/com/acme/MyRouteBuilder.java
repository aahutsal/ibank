package com.acme;

import java.util.Map;
import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.language.JsonPathExpression;


/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {
    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {
	// ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
	// // Note we can explicit name the component
	// getContext().addComponent("jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        // from("netty:tcp://0.0.0.0:6789?sync=true&textline=true&receiveBufferSize=8192&decoderMaxLineLength=8192")
	//     .log("Received: ${body}")
	//     .beanRef("main", "execCommand");
    
        // from("websocket://0.0.0.0:8443/command?sslContextParametersRef=#sslContextParameters&staticResources=classpath:.")
	//     .log("Received: ${body}")
	//     .beanRef("main", "execCommand");


        // from("jms:queue:facebook.ratings")
	//     .log("Facebook Rating received: ${headers}")
	//     .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))	    
	//     .setHeader(Exchange.HTTP_QUERY, simple("q=${headers.text}&key=" + keyGoogle))
	//     //.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
	//     //.setHeader("X-HTTP-Method-Override", constant("GET"))
	//     //.setBody(simple("q=${headers.text}"))
	//     .to("https4://www.googleapis.com/language/translate/v2/detect")
	//     //.beanRef("languageDetector", "detect")
	//     .split(new JsonPathExpression("$.data.detections"))
	//     // .processor(new Processor(){
	//     // 	    public void process(Exchange ex){
	//     // 		String lang = ((Map<String, String>)((net.minidev.json.JSONArray)ex.getIn().getBody()).get(0)).get("language").toString();
	//     // 		ex.getIn().setHeader("lang", lang);
	//     // 	    }
	//     // 	})
	//     .setHeader("lang", simple("${body[0][language]}"))
	//     .log("LANGUAGE: ${headers.lang}")
	//     .beanRef("facebookRating", "chooseComment")
	//     .to("jms:queue:bot.command");

	// onException(org.apache.camel.CamelExecutionException.class)
	//     .to("log:com.acme?showAll=true&multiline=true&showException=true&showStackTrace=true");

        from("seda:bank.send")
	    .to("log:com.acme?showAll=true&multiline=true&showStackTrace=true")
	    .to("file:target/bank/send/");

        from("direct:bank.receive")
	    .log("${body}")
	    .to("file:target/bank/receive/");

        from("direct:bank.withdraw")
	    .log("${body}")
	    .to("file:target/bank/withdraw/");
    }

}
