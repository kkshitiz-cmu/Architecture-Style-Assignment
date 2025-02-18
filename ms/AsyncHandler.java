import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class AsyncHandler extends Handler {
    private final Handler delegate;
    private final BlockingQueue<LogRecord> queue;
    private final Thread worker;
    private volatile boolean running = true;

    public AsyncHandler(Handler delegate) {
        this.delegate = delegate;
        this.queue = new LinkedBlockingQueue<>();
        
        this.worker = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    LogRecord record = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (record != null) {
                        delegate.publish(record);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    System.err.println("AsyncHandler error: " + ex.getMessage());
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) return;
        queue.offer(record);
    }

    @Override
    public void flush() {
        delegate.flush();
    }

    @Override
    public void close() throws SecurityException {
        running = false;
        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        delegate.close();
    }
}