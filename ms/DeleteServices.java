import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.sql.*;

public class DeleteServices extends UnicastRemoteObject implements DeleteServicesAI
{
    // Set up the JDBC driver name and database URL
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";
    static final String DB_URL = Configuration.getJDBCConnection();

    // Set up the orderinfo database credentials
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    // Do nothing constructor
    public DeleteServices() throws RemoteException {}

    // Main service loop
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
                System.out.println("\\t" + name);
            }
        } catch (Exception e) {
            System.out.println("DeleteServices binding err: " + e.getMessage());
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
            }

            stmt.close();
            conn.close();

        } catch(Exception e) {
            ReturnString = e.toString();
        }

        return(ReturnString);
    }
}
