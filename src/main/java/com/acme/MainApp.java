3ipackage com.acme;

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

import org.apache.commons.cli.*;

import com.mongodb.Mongo;
/**
 * A Camel Application
 */
public class MainApp {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */    
    public static void main(String... args) throws Exception {
	// create Options object
	Options options = new Options();

	// add t option
	options.addOption("das", false, "DAS server running");
	options.addOption("ibank", false, "iBank server running");
	options.addOption("activemq", true, "ActiveMQ URL");

	CommandLineParser parser = new DefaultParser();
	CommandLine cmd = parser.parse(options, args);

				    
	// create a Main instance
	Main main = new Main();
	// enable hangup support so you can press ctrl + c to terminate the JVM
	main.enableHangupSupport();
	String activemqUrl = "tcp://localhost:61616";
	
	if(cmd.hasOption("activemq")){
	    activemqUrl = cmd.getOptionValue("activemq");
	}
	    
	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activemqUrl);

	//bind main bean into the registery
	main.bind("main", new MainApp());
	main.bind("mongoBean", new com.mongodb.Mongo("localhost", 27017));

	if(cmd.hasOption("das")){
	    main.addRouteBuilder(new DASBankRouteBuilder(connectionFactory));	    
	}
	if(cmd.hasOption("ibank")){
	    main.addRouteBuilder(new IBankRouteBuilder(connectionFactory));
	}
	
	System.out.println("Starting Camel. Use ctrl + c to terminate the JVM.\n");
	main.run(args);
    }

}

