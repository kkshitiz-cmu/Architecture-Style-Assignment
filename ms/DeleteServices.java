import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.sql.*;

public class DeleteServices extends UnicastRemoteObject implements DeleteServicesAI
{ 
    
    // Add AuthServices field
    private Remote authServices;
    
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

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
    }

    public static void main(String args[]) 
    { 
        try 
        { 
            DeleteServices obj = new DeleteServices();
            Registry registry = Configuration.createRegistry();
            registry.bind("DeleteServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
            }
        } catch (Exception e) {
            System.out.println("DeleteServices binding err: " + e.getMessage()); 
            e.printStackTrace();
        } 
    }

    public String deleteOrder(String orderid, String token, String username) throws RemoteException
    {
        Connection conn = null;
        Statement stmt = null;
        String ReturnString = "Order Deleted Successfully";

        AuthServicesAI auth = (AuthServicesAI) authServices;

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
            }

            stmt.close();
            conn.close();

        } catch(Exception e) {
            ReturnString = e.toString();
        } 
        
        return(ReturnString);
    }
}