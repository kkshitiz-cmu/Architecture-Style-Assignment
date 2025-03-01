/******************************************************************************************************************
* File: RetrieveServices.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2018 Carnegie Mellon University
* Versions:
*	1.0 February 2018 - Initial write of assignment 3 (ajl).
*
* Description: This class provides the concrete implementation of the retrieve micro services. These services run
* in their own process (JVM).
*
* Parameters: None
*
* Internal Methods:
*  String retrieveOrders() - gets and returns all the orders in the orderinfo database
*  String retrieveOrders(String id) - gets and returns the order associated with the order id
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

public class RetrieveServices extends UnicastRemoteObject implements RetrieveServicesAI
{ 
    // Set up the JDBC driver name and database URL
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();

    // Set up the orderinfo database credentials
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    // Add AuthServices field
    private Remote authServices;
    private Remote loggingServices;

    public RetrieveServices() throws RemoteException {
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
            loggingServices =  registry.lookup("LoggingServices");
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
        // LoggingServicesAI logger = (LoggingServicesAI) loggingServices;

        try 
        { 
            RetrieveServices obj = new RetrieveServices();

            Registry registry = Configuration.createRegistry();
            registry.bind("RetrieveServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
            }

        } catch (Exception e) {

            System.out.println("RetrieveServices binding err: " + e.getMessage()); 
            e.printStackTrace();
        } 

    } // main


    // Inplmentation of the abstract classes in RetrieveServicesAI happens here.

    // This method will return all the entries in the orderinfo database

    public String retrieveOrders(String itoken, String iusername) throws RemoteException
    {
      	// Local declarations

        Connection conn = null;		// connection to the orderinfo database
        Statement stmt = null;		// A Statement object is an interface that represents a SQL statement.
        String ReturnString = "[";	// Return string. If everything works you get an ordered pair of data
        							// if not you get an error string

        AuthServicesAI auth = (AuthServicesAI) authServices;
        LoggingServicesAI logger = (LoggingServicesAI) loggingServices;

        // Validate that the token belongs to this user
        if (!auth.validateToken(itoken, iusername)) {
            return "Unauthorized: Token does not match user";
        }

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

            // System.out.println("Creating statement...");
            stmt = conn.createStatement();
            
            String sql;
            sql = "SELECT * FROM orders";
            ResultSet rs = stmt.executeQuery(sql);

            //Extract data from result set

            while(rs.next())
            {
                //Retrieve by column name
                int id  = rs.getInt("order_id");
                String date = rs.getString("order_date");
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String address = rs.getString("address");
                String phone = rs.getString("phone");

                //Display values
                //System.out.print("ID: " + id);
                //System.out.print(", date: " + date);
                //System.out.print(", first: " + first);
                //System.out.print(", last: " + last);
                //System.out.print(", address: " + address);
                //System.out.println("phone:"+phone);

                ReturnString = ReturnString +"{order_id:"+id+", order_date:"+date+", first_name:"+first+", last_name:"
                               +last+", address:"+address+", phone:"+phone+"}";

            }

            ReturnString = ReturnString +"]";
            logger.log("RetrieveServices", "Orders retrieved successfully: " + ReturnString, Level.INFO);

            //Clean-up environment

            rs.close();
            stmt.close();
            conn.close();
            stmt.close(); 
            conn.close();

        } catch(Exception e) {

            ReturnString = e.toString();
            logger.log("RetrieveServices", "Error retrieving orders: " + e.getMessage(), Level.SEVERE);
        } 
        
        return(ReturnString);

    } //retrieve all orders

    // This method will returns the order in the orderinfo database corresponding to the id
    // provided in the argument.

    public String retrieveOrders(String iorderid, String itoken, String iusername) throws RemoteException
    {
      	// Local declarations

        Connection conn = null;		// connection to the orderinfo database
        Statement stmt = null;		// A Statement object is an interface that represents a SQL statement.
        String ReturnString = "[";	// Return string. If everything works you get an ordered pair of data
        							// if not you get an error string
        
        AuthServicesAI auth = (AuthServicesAI) authServices;
        LoggingServicesAI logger = (LoggingServicesAI) loggingServices;

        // Validate that the token belongs to this user
        if (!auth.validateToken(itoken, iusername)) {
            return "Unauthorized: Token does not match user";
        }

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

            // System.out.println("Creating statement...");
            stmt = conn.createStatement();
            
            String sql;
            sql = "SELECT * FROM orders where order_id=" + iorderid;
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set. Note there should only be one for this method.
            // I used a while loop should there every be a case where there might be multiple
            // orders for a single ID.

            while(rs.next())
            {
                //Retrieve by column name
                int id  = rs.getInt("order_id");
                String date = rs.getString("order_date");
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String address = rs.getString("address");
                String phone = rs.getString("phone");

                //Display values
                //System.out.print("ID: " + id);
                //System.out.print(", date: " + date);
                //System.out.print(", first: " + first);
                //System.out.print(", last: " + last);
                //System.out.print(", address: " + address);
                //System.out.println("phone:"+phone);

                ReturnString = ReturnString +"{order_id:"+id+", order_date:"+date+", first_name:"+first+", last_name:"
                               +last+", address:"+address+", phone:"+phone+"}";
            }

            ReturnString = ReturnString +"]";
            logger.log("RetrieveServices", "Order retrieved for ID " + iorderid + " : " + ReturnString, Level.INFO);

            //Clean-up environment

            rs.close();
            stmt.close();
            conn.close();
            stmt.close(); 
            conn.close();

        } catch(Exception e) {

            ReturnString = e.toString();
            logger.log("RetrieveServices", "Error retrieving order for ID: " + iorderid + " - " + e.getMessage(), Level.SEVERE);

        } 

        return(ReturnString);

    } //retrieve order by id

} // RetrieveServices