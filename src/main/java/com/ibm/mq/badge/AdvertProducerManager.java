package com.ibm.mq.badge;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.TextMessage;
import jakarta.xml.bind.JAXBException;
import com.ibm.mq.events.Advert;


public class AdvertProducerManager {
    private static final Logger logger = Logger.getLogger("com.ibm.mq.badge");
    private static final String TOPIC_NAME = "newTickets";
    private final JMSContext context;
    private final JMSProducer newTicketsTopicProducer;
    private final Destination destinationTopic;

    public AdvertProducerManager(ConnectionManager connectionManager) throws JMSException {
        context = connectionManager.connectAndGetConnection();

        // !!! createTopic
        destinationTopic = context.createTopic(TOPIC_NAME);
        newTicketsTopicProducer = context.createProducer();
    }

    public void publishAdvert(Advert event) throws JAXBException {
        TextMessage message = context.createTextMessage(event.toXML());
        newTicketsTopicProducer.send(destinationTopic, message);
    }

    public void close() {
        try {
            if (context != null) {
                context.close();
            }
        } catch (JMSRuntimeException e) {
            logger.log(Level.SEVERE, "Could not close session for AdvertManager", e);
        }
    }
}