import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.logging.Logger;
import java.lang.management.ManagementFactory;
import java.sql.*;

public class DeleteServices extends UnicastRemoteObject implements DeleteServicesAI
{ 
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    //Create logger class
    private static final Logger logger = LoggerUtil.getLogger("DeleteServices_"+ManagementFactory.getRuntimeMXBean().getName());

    public DeleteServices() throws RemoteException {}

    public static void main(String args[]) 
    { 
        try 
        { 
            DeleteServices obj = new DeleteServices();
            Registry registry = Configuration.createRegistry();
            registry.bind("DeleteServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            logger.info("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
                logger.info("\t" + name);
            }
        } catch (Exception e) {
            System.out.println("DeleteServices binding err: " + e.getMessage()); 
            logger.severe("DeleteServices binding err: " + e.getMessage()); 
            e.printStackTrace();
        } 
    }

    public String deleteOrder(String orderid) throws RemoteException
    {
        Connection conn = null;
        Statement stmt = null;
        String ReturnString = "Order Deleted Successfully";

        try
        {
            Class.forName(JDBC_CONNECTOR);
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            stmt = conn.createStatement();
            
            String sql = "DELETE FROM orders WHERE order_id=" + orderid;
            int result = stmt.executeUpdate(sql);

            if(result == 0) {
                ReturnString = "No order found with ID: " + orderid;
                logger.info("No order found with ID: " + orderid);
            }else{
                logger.info("Order deleted successfully with ID: " + orderid);
            }



            stmt.close();
            conn.close();

        } catch(Exception e) {
            ReturnString = e.toString();
            logger.severe("Error deleting order: " + e.getMessage());
        } 
        
        return(ReturnString);
    }
}