package com.acme;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
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
import org.apache.camel.Main;
import org.springframework.core.io.ClassPathResource;


import javax.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import com.mongodb.Mongo;
/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */    
    public static void main(String... args) throws Exception {
	// create a Main instance
	Main main = new Main();
	// enable hangup support so you can press ctrl + c to terminate the JVM
	main.enableHangupSupport();
	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
	System.out.println("CF:" + connectionFactory);

	//bind main bean into the registery
	main.bind("main", new MainApp());
	main.bind("mongoBean", new com.mongodb.Mongo("localhost", 27017));
	
	main.addRouteBuilder(new IBankRouteBuilder(connectionFactory));
	main.addRouteBuilder(new DASBankRouteBuilder(connectionFactory));
	System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
	main.run(args);
    }

}

