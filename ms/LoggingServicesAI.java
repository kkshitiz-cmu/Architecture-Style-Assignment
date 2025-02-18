import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.Level;

public interface LoggingServicesAI extends java.rmi.Remote {
    void log(String serviceName, String message, Level level) throws RemoteException;
}