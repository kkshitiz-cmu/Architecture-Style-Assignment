import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.sql.*;

public interface AuthServicesAI extends java.rmi.Remote {
    /**
     * Authenticates a user and returns a JWT token if successful
     */
    String login(String username, String password) throws RemoteException;

    Boolean validateToken(String token, String username) throws RemoteException;

    String getUsernameFromToken(String token) throws RemoteException;
}