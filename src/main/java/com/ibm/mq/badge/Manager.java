package com.ibm.mq.badge;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import jakarta.xml.bind.JAXBException;
import com.ibm.mq.LoggerInitializer;
import com.ibm.mq.events.Advert;

public class Manager {

    private static final Logger logger = Logger.getLogger("com.ibm.mq.badge");

    public static void main(String[] args) throws JAXBException, JMSException, FileNotFoundException {
        Thread.currentThread().setName("Main");
        LoggerInitializer.initialiseLogs();
        ConnectionManager connectionManager = new ConnectionManager();

        AdvertInitializer advertInitializer = new AdvertInitializer();
        Cashier cashier = new Cashier(connectionManager);
        AdvertProducerManager advertProducerManager = new AdvertProducerManager(connectionManager);

        new Thread(cashier).start();

        while (true) {
            if (cashier.isListeningToResponse()) {

                // For every event created, publish it in 30 second intervals
                for (Advert advert : advertInitializer.getBookableAdverts().values()) {
                    logger.info("-----");
                    logger.info("Publishing advert...");

                    // Avoids an illegal reflective access operation caused by jaxb dependencies
                    final String key = "org.glassfish.jaxb.runtime.v2.bytecode.ClassTailor.noOptimize";
                    System.setProperty(key, "true");

                    advertProducerManager.publishAdvert(advert);

                    logger.info("Advert for '" + advert.getTitle() + "' has been published");
                    logger.fine("Waiting for 30 seconds before publishing next advert");
                    logger.info("-----");
                    try {
                        // Sleep for 30 seconds before publishing the next event
                        Thread.sleep(30_000);
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Interrupted the 10 second wait inbetween publishing adverts", e);
                    }
                }
                // Break out of the while loop after all events have been published.
                break;
            } else {
                try {
                    // If the cashier is not ready yet then wait 1 second.
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    logger.info("Could not sleep for 1 second");
                    e.printStackTrace();
                }
            }
        }

        advertProducerManager.close();
        cashier.close();
        connectionManager.closeConnections();
    }

}