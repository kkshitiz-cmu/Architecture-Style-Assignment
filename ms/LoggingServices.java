import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.*;
import java.rmi.registry.Registry;
import java.io.IOException;

public class LoggingServices extends UnicastRemoteObject implements LoggingServicesAI {

    // Create a common logger that writes to a shared log file asynchronously.
    private static final Logger logger = createAsyncLogger();

    protected LoggingServices() throws RemoteException {
        super();
    }

    // Main service loop
    public static void main(String args[]) {
        try {
            LoggingServices obj = new LoggingServices();
            Registry registry = Configuration.createRegistry();
            registry.bind("LoggingServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            // logger.info("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
                // logger.info("\t" + name);
            }
        } catch (Exception e) {
            // logger.severe("LoggingServices binding err:: " + e.getMessage()); 
            System.out.println("LoggingServices binding err: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void log(String serviceName, String message, Level level) throws RemoteException {
        // Prepend the service name to the log message
        logger.log(level, "[" + serviceName + "] " + message);
    }
    
    private static Logger createAsyncLogger() {
        Logger log = Logger.getLogger("GlobalLoggingService");
        log.setUseParentHandlers(false);

        if (log.getHandlers().length == 0) {
            try {
                // Use a fixed file name for the shared log file.
                FileHandler fileHandler = new FileHandler("global.log", true);
                fileHandler.setFormatter(new SimpleFormatter());
                
                // Wrap the FileHandler with our custom AsyncHandler.
                AsyncHandler asyncHandler = new AsyncHandler(fileHandler);
                log.addHandler(asyncHandler);
                log.setLevel(Level.INFO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return log;
    }
}
