package equipable.me.disconsole;

import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LogAppender extends AbstractAppender {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public LogAppender() {
        super("LogAppender", null, null, false, Property.EMPTY_ARRAY);
    }
    
    @Override
    public void append(LogEvent logEvent) {
        
        String formattedTime = LocalDateTime.now().format(formatter);
        String formattedMsg = logEvent.getMessage().getFormattedMessage();
        String cleanedLogMsg = COLOR_CODE_PATTERN.matcher(formattedMsg).replaceAll("");
        cleanedLogMsg = HEX_PATTERN.matcher(cleanedLogMsg).replaceAll("");
        cleanedLogMsg = ANSI_ESCAPE_PATTERN.matcher(cleanedLogMsg).replaceAll("");
        cleanedLogMsg = WHITESPACE_PATTERN.matcher(cleanedLogMsg).replaceAll(" ");
        String loggerName = logEvent.getLoggerName() == null || logEvent.getLoggerName().isEmpty() ? "Server" : logEvent.getLoggerName();
        var split = loggerName.split("\\.");
        if (split.length != 0) {
            loggerName = split[split.length - 1];
        }
        Level loglevel = logEvent.getLevel();
        String logMessage = "[" + formattedTime + " " + loglevel + "] " + "[" + loggerName + "] " + cleanedLogMsg;
        RateLimitHandler.insertNewLog(logMessage + "\n");

    }
    
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("[&§][0-9A-FK-ORX]", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEX_PATTERN = Pattern.compile("&x((?:&\\p{XDigit}){6})", Pattern.CASE_INSENSITIVE);
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\u001B\\[[;\\d]*m", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
}