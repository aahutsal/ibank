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
/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    private static final AtomicBoolean shutdownCalled = new AtomicBoolean(false);
    
    public static void main(String... args) throws Exception {
	// create a Main instance
	Main main = new Main();
	// enable hangup support so you can press ctrl + c to terminate the JVM
	main.enableHangupSupport();

	//initializeEngine(main);
	//initializeProxy();
	// bind MyBean into the registery	
	main.bind("main", new MainApp());
	main.addRouteBuilder(new MyRouteBuilder());
	System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
	main.run(args);
    }

}

