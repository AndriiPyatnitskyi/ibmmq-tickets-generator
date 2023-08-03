package com.ibm.mq.badge;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;

import com.ibm.mq.MQProperties;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Deals with connections to MQ. This class is responsible for the config and creation of connections to topics or queues. The default file requested by this
 * class must obey the following:
 * <ul>
 * <li>Follow the <b>JSON</b> format.
 * <li>Provide tags:
 *  <ul>
 *  <li><tt>HOST</tt>
 *  <li><tt>PORT</tt>
 *  <li><tt>CHANNEL</tt>
 *  <li><tt>QMGR</tt>
 *  <li><tt>USER</tt>
 *  <li><tt>PASSWORD</tt>
 *  <li><tt>SUBSCRIPTION_NAME</tt>
 *  </ul>
 * </li>
 * </ul>
 *
 * @author Benjamin Brunyee
 * @version 1.0
 */

@NoArgsConstructor
@Setter
@Getter
public class ConnectionManager {
    private static final Logger logger = Logger.getLogger("com.ibm.mq.badge");

    private final ArrayList<JMSContext> openContexts = new ArrayList<>();

    public JMSContext connectAndGetConnection() throws JMSException {
        JmsFactoryFactory jmsFactoryFactory = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
        JmsConnectionFactory connectionFactory = jmsFactoryFactory.createConnectionFactory();

        connectionFactory.setStringProperty(WMQConstants.WMQ_HOST_NAME, MQProperties.HOST);
        connectionFactory.setIntProperty(WMQConstants.WMQ_PORT, MQProperties.PORT);
        connectionFactory.setStringProperty(WMQConstants.WMQ_CHANNEL, MQProperties.CHANNEL);
        connectionFactory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
        connectionFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
        connectionFactory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, MQProperties.QUEUE_MANAGER);

        //todo read about ACKNOWLEDGE types
        JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE);
        context.start();

        openContexts.add(context);

        return context;
    }

    public void closeConnections() {
        logger.finer("Attempting to close all open connections for EnvSetter");
        for (JMSContext context : openContexts) {
            if (context != null) {
                context.close();
            }
        }
    }

}
