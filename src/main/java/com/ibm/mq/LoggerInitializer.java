package com.ibm.mq;

import java.io.IOException;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class LoggerInitializer {
    private static final Level LOGLEVEL = Level.ALL;

    private static final Logger logger = Logger.getLogger("com.ibm.mq.badge");

    public static void initialiseLogs() {
        // Removing the initial handler for the logger
        Logger defaultLogger = Logger.getLogger("");
        Handler[] handlers = defaultLogger.getHandlers();
        defaultLogger.removeHandler(handlers[0]);

        /*
         * Setting the formatter in which logging will be printed to:
         * [DATE TIME]-[THREADNAME]-[LOGLEVEL]-[CLASS]-[METHOD]: Message
         */
        Formatter formatter = new SimpleFormatter() {
            private static final String FORMAT = "[%1$tF %1$tT]-[%2$-7s]-[%3$-7s]-[%4$-14s]-[%5$-19s]: %6$s %n";

            private boolean throwing = false; // The log is not throwing an exception by default.
            private int stackTraceElement = 8; // Default stack trace element.

            @Override
            public synchronized String format(LogRecord lr) {
                isThrowing(lr); // Is the log record including a throwable exception.
                return String.format(FORMAT,
                    new Date(lr.getMillis()),
                    Thread.currentThread().getName(),
                    lr.getLevel().getLocalizedName(),
                    Thread.currentThread().getStackTrace()[stackTraceElement].getClassName().toString().replace("com.ibm.mq.badge.", ""),
                    Thread.currentThread().getStackTrace()[stackTraceElement].getMethodName(),
                    getMessage(lr)
                );
            }

            // Is the log record including a throwable exception.
            private void isThrowing(LogRecord lr) {
                if (lr.getThrown() != null) {
                    throwing = true;
                    stackTraceElement = 7;
                }
            }

            // If log record is inlucding an exception then add stack trace to the message.
            private String getMessage(LogRecord lr) {
                String message = lr.getMessage();
                if (throwing) {
                    for (StackTraceElement str : lr.getThrown().getStackTrace()) {
                        message += "\n" + str;
                    }
                }
                return message;
            }
        };

        Formatter consoleFormatter = new SimpleFormatter() {
            private static final String FORMAT = "[%1$tF %1$tT]-[%2$-7s]: %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(FORMAT,
                    new Date(lr.getMillis()),
                    Thread.currentThread().getName(),
                    getMessage(lr)
                );
            }

            // If log record is inlucding an exception then add stack trace to the message.
            private String getMessage(LogRecord lr) {
                String message = lr.getMessage();
                if (lr.getThrown() != null) {
                    for (StackTraceElement str : lr.getThrown().getStackTrace()) {
                        message += "\n" + str;
                    }
                }
                return message;
            }
        };

        // Writing the log to the console
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(consoleFormatter);
        consoleHandler.setLevel(LOGLEVEL);

        // Writing the log to a file
        try {
            FileHandler fileHandler = new FileHandler("ResellerLog.txt");
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(LOGLEVEL);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        logger.addHandler(consoleHandler);
        logger.addHandler(new SevereHandler());

        logger.setLevel(LOGLEVEL);
        logger.finest("Logger initialised");
    }

    public static class SevereHandler extends StreamHandler {
        /**
         * Print the stack trace everytime the application runs into a severe
         * log message.
         * @param record The message to be logged.
         */
        @Override
        public void publish(LogRecord record) {
            // Print the message with the current logger config.
            super.publish(record);
            if (record.getLevel().equals(Level.SEVERE)) {
                System.exit(1);
            }
        }
    }
}
