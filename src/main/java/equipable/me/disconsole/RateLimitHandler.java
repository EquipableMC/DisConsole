package equipable.me.disconsole;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RateLimitHandler {
    static StringBuilder currentMessage = new StringBuilder();
    static final int maxLength = 1920;
    static boolean isPending = false;
    static final int maxTimeBetweenLogs = 5; // seconds
    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public synchronized static void insertNewLog(String message) {
        if (message == null || message.isEmpty()) return;

        currentMessage.append(message);
        if (!isPending) {
            isPending = true;
            executorService.submit(RateLimitHandler::processMessages);
        }
    }

    private static void processMessages() {
        while (isPending) {
            synchronized (RateLimitHandler.class) {
                String message = currentMessage.toString();
                if (message.length() >= maxLength) {
                    String cutMessage1 = message.substring(0, maxLength);
                    String cutMessage2 = message.substring(maxLength);
                    handleSend(cutMessage1);
                    currentMessage = new StringBuilder(cutMessage2);
                } else {
                    handleSend(message);
                    currentMessage = new StringBuilder();
                    isPending = false;
                }

                if (currentMessage.length() > 0) {
                    isPending = true; // Ensure loop continues if more messages are pending
                }
            }

            // Wait for the next cycle (20 ticks = 1 second)
            try {
                Thread.sleep(maxTimeBetweenLogs * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Exit if the thread is interrupted
            }
        }
    }

    private synchronized static void handleSend(String message) {
        String finalmsg = "```" +
                message +
                "```";
        DisConsole.sendMessage(finalmsg);
    }

    public static void resetRateLimiter() {
        synchronized (RateLimitHandler.class) {
            // Shut down the existing executor service
            shutdownExecutorService();
            executorService = Executors.newSingleThreadExecutor();

            // Reset state variables
            currentMessage = new StringBuilder();
            isPending = false;
        }
    }
    public static void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}

