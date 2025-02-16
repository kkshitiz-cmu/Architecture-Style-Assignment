import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Logger {
    private PrintWriter logger;
    private String logFileName;
    
    public Logger(String logFileName) {
        this.logFileName = logFileName;
        try {
            logger = new PrintWriter(new FileWriter(logFileName, true));
        } catch (IOException e) {
            System.err.println("Could not initialize logger: " + e.getMessage());
        }
    }
    
    public void log(String message) {
        String logMessage = LocalDateTime.now() + " - " + message;
        System.out.println(logMessage);
        if (logger != null) {
            logger.println(logMessage);
            logger.flush();
        }
    }
    
    public void close() {
        if (logger != null) {
            logger.close();
        }
    }
}
