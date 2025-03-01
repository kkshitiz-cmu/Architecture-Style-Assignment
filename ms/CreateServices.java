/******************************************************************************************************************
* File: CreateServices.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2018 Carnegie Mellon University
* Versions:
*	1.0 February 2018 - Initial write of assignment 3 (ajl).
*
* Description: This class provides the concrete implementation of the create micro services. These services run
* in their own process (JVM).
*
* Parameters: None
*
* Internal Methods:
*  String newOrder() - creates an order in the ms_orderinfo database from the supplied parameters.
*
* External Dependencies: 
*	- rmiregistry must be running to start this server
*	= MySQL
	- orderinfo database 
******************************************************************************************************************/
import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.sql.*;

public class CreateServices extends UnicastRemoteObject implements CreateServicesAI
{ 
    // Add AuthServices field
    private Remote authServices;
    private Remote loggingServices;
    // Set up the JDBC driver name and database URL
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();

    // Set up the orderinfo database credentials
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    // Do nothing constructor
    public CreateServices() throws RemoteException {
        super();
        try {
            // Get the auth service from registry
            Registry registry = LocateRegistry.getRegistry("ms_auth", 1097);
            authServices = registry.lookup("AuthServices");
        } catch (Exception e) { 
            System.out.println("Error connecting to AuthServices: " + e.getMessage());
            throw new RemoteException("Could not initialize auth services");
        }
        try {
            // Look up the centralized logging service.
            Registry registry = LocateRegistry.getRegistry("ms_logging", 1100);
            loggingServices = registry.lookup("LoggingServices");
        } catch (Exception e) {
            System.out.println("Error connecting to LoggingServices: " + e.getMessage());
            throw new RemoteException("Could not initialize logging service");
        }
    }

    // Main service loop
    public static void main(String args[]) 
    { 	
    	// What we do is bind to rmiregistry, in this case localhost, port 1099. This is the default
    	// RMI port. Note that I use rebind rather than bind. This is better as it lets you start
    	// and restart without having to shut down the rmiregistry. 

        try 
        { 
            CreateServices obj = new CreateServices();

            Registry registry = Configuration.createRegistry();
            registry.bind("CreateServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
            }
            // Bind this object instance to the name RetrieveServices in the rmiregistry 
            // Naming.rebind("//" + Configuration.getRemoteHost() + ":1099/CreateServices", obj); 

        } catch (Exception e) {

            System.out.println("CreateServices binding err: " + e.getMessage());
            e.printStackTrace();
        } 

    } // main


    // Inplmentation of the abstract classes in RetrieveServicesAI happens here.

    // This method add the entry into the ms_orderinfo database

    public String newOrder(String idate, String ifirst, String ilast, String iaddress, String iphone, String itoken, String iusername) throws RemoteException
    {
        AuthServicesAI auth = (AuthServicesAI) authServices;
        LoggingServicesAI logger = (LoggingServicesAI) loggingServices;

        // Validate that the token belongs to this user
        if (!auth.validateToken(itoken, iusername)) {
            return "Unauthorized: Token does not match user";
        }
      	// Local declarations

        Connection conn = null;		                 // connection to the orderinfo database
        Statement stmt = null;		                 // A Statement object is an interface that represents a SQL statement.
        String ReturnString = "Order Created";	     // Return string. If everything works you get an 'OK' message
        							                 // if not you get an error string
        try
        {
            // Here we load and initialize the JDBC connector. Essentially a static class
            // that is used to provide access to the database from inside this class.

            Class.forName(JDBC_CONNECTOR);

            //Open the connection to the orderinfo database

            //System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // Here we create the queery Execute a query. Not that the Statement class is part
            // of the Java.rmi.* package that enables you to submit SQL queries to the database
            // that we are connected to (via JDBC in this case).

            stmt = conn.createStatement();
            
            String sql = "INSERT INTO orders(order_date, first_name, last_name, address, phone) VALUES (\""+idate+"\",\""+ifirst+"\",\""+ilast+"\",\""+iaddress+"\",\""+iphone+"\")";

            // execute the update

            stmt.executeUpdate(sql);

            logger.log("CreateServices", "New order created: " + sql, Level.INFO);

            // clean up the environment

            stmt.close();
            conn.close();
            stmt.close(); 
            conn.close();

        } catch(Exception e) {

            ReturnString = e.toString();
            logger.log("CreateServices", "Error creating order: " + e.getMessage(), Level.SEVERE);
        } 
        
        return(ReturnString);

    } //retrieve all orders

} // RetrieveServices