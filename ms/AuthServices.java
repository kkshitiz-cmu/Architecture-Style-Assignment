import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AuthServices extends UnicastRemoteObject implements AuthServicesAI {
    
    private Remote loggingServices;
    
    // Set up the JDBC driver name and database URL
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();

    // Database credentials
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    // Store active tokens: Map<token, username>
    private static final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    // Do nothing constructor
    public AuthServices() throws RemoteException {
        super();
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
    public static void main(String args[]) {
        try {
            AuthServices obj = new AuthServices();
            Registry registry = Configuration.createRegistry();
            registry.bind("AuthServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
            }
        } catch (Exception e) {
            System.out.println("AuthServices binding err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String login(String username, String password) throws RemoteException {
        Connection conn = null;
        Statement stmt = null;
        String token = null;
        LoggingServicesAI logger = (LoggingServicesAI) loggingServices;

        try {
            Class.forName(JDBC_CONNECTOR);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            
            String sql = "SELECT * FROM users WHERE user_id='" + username + "' AND password='" + password + "'";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                token = generateToken();
                activeTokens.put(token, username);
                logger.log("AuthService","User " + username + " logged in.",Level.INFO);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch(Exception e) {
            logger.log("AuthService","Error logging in user" + username,Level.SEVERE);
            throw new RemoteException(e.toString());
        }

        return token;
    }

    public Boolean validateToken(String token, String username) throws RemoteException {
        LoggingServicesAI logger = (LoggingServicesAI) loggingServices;
        try {
        // Get the username associated with the token
        String tokenUsername = activeTokens.get(token);
        
        // Check if token exists and belongs to the specified user
        return tokenUsername != null && tokenUsername.equals(username);
        } catch(Exception e) {
            logger.log("AuthService","Invalid token from user "+ username,Level.SEVERE);
            throw new RemoteException("Invalid token");
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String getUsernameFromToken(String token) throws RemoteException {
        return activeTokens.get(token);
    }
}