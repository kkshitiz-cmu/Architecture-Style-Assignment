import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.lang.management.ManagementFactory;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class AuthServices extends UnicastRemoteObject implements AuthServicesAI {
    // Set up the JDBC driver name and database URL
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();

    // Database credentials
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    // Store active tokens: Map<token, username>
    private static final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    //Create logger class
    private static final Logger logger = LoggerUtil.getLogger("CreateServices_"+ManagementFactory.getRuntimeMXBean().getName());

    // Do nothing constructor
    public AuthServices() throws RemoteException {}

    // Main service loop
    public static void main(String args[]) {
        try {
            AuthServices obj = new AuthServices();
            Registry registry = Configuration.createRegistry();
            registry.bind("AuthServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            logger.info("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
                logger.info("\t" + name);
            }
        } catch (Exception e) {
            logger.severe("AuthServices binding err:: " + e.getMessage()); 
            System.out.println("AuthServices binding err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String login(String username, String password) throws RemoteException {
        Connection conn = null;
        Statement stmt = null;
        String token = null;

        try {
            Class.forName(JDBC_CONNECTOR);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            
            String sql = "SELECT * FROM users WHERE user_id='" + username + "' AND password='" + password + "'";
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                token = generateToken();
                activeTokens.put(token, username);
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch(Exception e) {
            throw new RemoteException(e.toString());
        }

        return token;
    }

    public Boolean validateToken(String token, String username) throws RemoteException {
        try {
        // Get the username associated with the token
        String tokenUsername = activeTokens.get(token);
        
        // Check if token exists and belongs to the specified user
        return tokenUsername != null && tokenUsername.equals(username);
        } catch(Exception e) {
            logger.severe("Invalid token from user "+ username);
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