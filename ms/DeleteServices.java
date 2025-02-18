import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.sql.*;

public class DeleteServices extends UnicastRemoteObject implements DeleteServicesAI
{ 
    
    // Add AuthServices field
    private Remote authServices;
    private Remote loggingServices;
    
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    //Create logger class
    // private static final Logger logger = LoggerUtil.getLogger("DeleteServices_"+ManagementFactory.getRuntimeMXBean().getName());
    
    public DeleteServices() throws RemoteException {
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

    public static void main(String args[]) 
    { 
        // LoggingServicesAI logger = (LoggingServicesAI) loggingServices;
        try 
        { 
            DeleteServices obj = new DeleteServices();
            Registry registry = Configuration.createRegistry();
            registry.bind("DeleteServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            // logger.info("Registered services:");
            // logger.log("DeleteServices", "Registered services:", Level.INFO);
            for (String name : boundNames) {
                System.out.println("\t" + name);
                // logger.info("\t" + name);
                // logger.log("DeleteServices", "\t" + name, Level.INFO);
            }
        } catch (Exception e) {
            System.out.println("DeleteServices binding err: " + e.getMessage()); 
            // logger.severe("DeleteServices binding err: " + e.getMessage()); 
            // logger.log("DeleteServices", "DeleteServices binding err: " + e.getMessage(), Level.SEVERE);
            e.printStackTrace();
        } 
    }

    public String deleteOrder(String orderid, String token, String username) throws RemoteException
    {
        Connection conn = null;
        Statement stmt = null;
        String ReturnString = "Order Deleted Successfully";

        AuthServicesAI auth = (AuthServicesAI) authServices;
        LoggingServicesAI logger = (LoggingServicesAI) loggingServices;

        // Validate that the token belongs to this user
        if (!auth.validateToken(token, username)) {
            return "Unauthorized: Token does not match user";
        }

        try
        {
            Class.forName(JDBC_CONNECTOR);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            stmt = conn.createStatement();
            
            String sql = "DELETE FROM orders WHERE order_id=" + orderid;
            int result = stmt.executeUpdate(sql);

            if(result == 0) {
                ReturnString = "No order found with ID: " + orderid;
                // logger.info("No order found with ID: " + orderid);
                logger.log("DeleteServices", "No order found with ID: " + orderid, Level.INFO);
            }else{
                // logger.info("Order deleted successfully with ID: " + orderid);
                logger.log("DeleteServices", "Order deleted successfully with ID: " + orderid, Level.INFO);
            }



            stmt.close();
            conn.close();

        } catch(Exception e) {
            ReturnString = e.toString();
            // logger.severe("Error deleting order: " + e.getMessage());
            logger.log("DeleteServices", "Error deleting order: " + e.getMessage(), Level.SEVERE);
        } 
        
        return(ReturnString);
    }
}