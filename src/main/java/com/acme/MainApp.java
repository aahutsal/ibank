package com.acme;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.camel.Main;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducerTemplate;    
import org.springframework.core.io.ClassPathResource;
/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    private static final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    
    private static final ScriptEngineManager manager = new ScriptEngineManager();

    private static final ScriptEngine engine = manager.getEngineByName("JavaScript");
    
    public static void main(String... args) throws Exception {
	// create a Main instance
	Main main = new Main();
	// enable hangup support so you can press ctrl + c to terminate the JVM
	main.enableHangupSupport();

	initializeEngine(main);
	//initializeProxy();
	// bind MyBean into the registery	
	main.bind("main", new MainApp());
	main.addRouteBuilder(new MyRouteBuilder());
	System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
	main.run(args);	
    }

    protected static void initializeEngine(Main main) 
	throws IOException, ScriptException, Exception{
	ProducerTemplate producer = new DefaultProducerTemplate(main.getOrCreateCamelContext());
	producer.start();
	engine.put("producer", producer);
	engine.eval(new InputStreamReader(new ClassPathResource("js/bank.js").getInputStream()));
    }
}

