package com.ibm.mq.badge;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import com.ibm.mq.MessageParser;
import com.ibm.mq.events.RequestTickets;

@Getter
@Setter
@RequiredArgsConstructor
public class Cashier implements Runnable {
    private static final String SEND_TO_QUEUE_NAME = "confirmation";
    private static final String RECEIVE_FROM_QUEUE_NAME = "purchase";
    private static final Logger logger = Logger.getLogger("com.ibm.mq.badge");
    private final ConnectionManager connectionManager;
    private boolean listeningToResponse;
    volatile boolean cancel = false;
    private JMSContext context;
    private Destination destinationQueue;
    private JMSConsumer purchaseConsumer;
    private JMSProducer confirmationProducer;

    @SneakyThrows
    public void run() {
        Thread.currentThread().setName("Cashier");
        connect();
        waitForRequest();
    }

    public void connect() throws JMSException {
        context = connectionManager.connectAndGetConnection();

        // Creating the consumer which messages will be received from.
        // !!! createQueue
        Queue queue = context.createQueue(RECEIVE_FROM_QUEUE_NAME);
        // !!! createConsumer
        purchaseConsumer = context.createConsumer(queue);

        // Creating the producer which messages will be sent to.
        // !!! createQueue
        destinationQueue = context.createQueue(SEND_TO_QUEUE_NAME);
        // !!! createProducer
        confirmationProducer = context.createProducer();

        logger.finer("Connection for cashier has been created");
    }

    public void waitForRequest() throws JMSException {
        cancel = false;
        logger.info("Starting to listen for ticket requests");
        while (!cancel) {
            listeningToResponse = true;
            // !!!  Wait 10 seconds for a message
            Message message = purchaseConsumer.receive(10_000); // Wait 10 seconds for a message. If no message is received, the method returns null.

            // Creates and sends a response depending on the message received.
            if (message != null) {
                logger.info("Received message");
                logger.info("Message received: " + message);
                processMessage(message);
            }
        }
        logger.info("Stopped listening to responses");
        listeningToResponse = false;
    }


    public void close() {
        cancel = true;
        logger.finest("Close flag set for cashier to signal for the connections to close");
        while (true) {
            if (!isListeningToResponse()) {
                closeConnection();
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Interrupted sleep when waiting to close connections", e);
                }
            }
        }
    }

    private void processMessage(Message message) throws JMSException {
        RequestTickets body = MessageParser.parse(message, RequestTickets.class);
        if (body == null) {
            logger.warning("body is null");
            return;
        }

        TextMessage responseMessage = context.createTextMessage("Accepted");
        responseMessage.setJMSCorrelationID(message.getJMSCorrelationID());

        confirmationProducer.send(destinationQueue, responseMessage);
        logger.info("Sent response");
    }

    private void closeConnection() {
        if (purchaseConsumer != null) {
            purchaseConsumer.close();
        }

        if (context != null) {
            context.close();
        }

        logger.finer("Connections for cashier have closed");
    }
}