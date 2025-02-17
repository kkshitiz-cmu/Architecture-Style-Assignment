import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {
    public static Logger getLogger(String serviceName) {
        // Create a logger with the service name
        Logger logger = Logger.getLogger(serviceName);
        // Remove any default handlers
        logger.setUseParentHandlers(false);
        
        // Check if handlers have already been added to avoid duplicates
        if (logger.getHandlers().length == 0) {
            try {
                // Create a FileHandler that writes to a file named after the service
                FileHandler fileHandler = new FileHandler(serviceName + ".log", true);
                fileHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(fileHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logger;
    }
}