package com.ibm.mq;


import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@NoArgsConstructor
public class MessageParser {
    private static final Logger logger = Logger.getLogger("com.ibm.mq");

//    private static final Logger LOGGER = LoggerFactory.getLogger(MessageParser.class);
    public static final String MSG_SUB_TYPE = "message_sub_type";
    public static final String MSG_PAYLOAD_VERSION = "message_payload_version";
    public static final String MSG_ID = "message_id";
    public static final String MSG_VAM_REQ_ID = "vam_request_id";


    public static <T> T parse(Message message, Class<T> type) {
        try {
            logger.info("Received Message: + " + message);

            if (!(message instanceof TextMessage)) {
                logger.info("Received jms message is not of type TextMessage");
                return null;
            }

            final TextMessage textMsg = (TextMessage) message;

            String msgId = (String) textMsg.getObjectProperty(MSG_ID);
            String jmsMessageId = textMsg.getJMSMessageID();
            String jmsCorrelationId = textMsg.getJMSCorrelationID();
            String vamRequestId = (String) textMsg.getObjectProperty(MSG_VAM_REQ_ID);
            String eventType = (String) textMsg.getObjectProperty(MSG_SUB_TYPE);
            String messagePayloadVersion = (String) textMsg.getObjectProperty(MSG_PAYLOAD_VERSION);

            logger.info(String.format(
                "Received Message information: " +
                    "message_id: %s, " +
                    "jms_message_id: %s, " +
                    "jms_correlation_id: %s, " +
                    "vam_request_id: %s, " +
                    "event_type: %s, " +
                    "payload_version: %s",
                msgId, jmsMessageId, jmsCorrelationId, vamRequestId, eventType, messagePayloadVersion));

            return new ObjectMapper()
                .readValue(textMsg.getText(), type);

        } catch (JMSException | JsonProcessingException ex) {
            logger.warning("Mesage Parsing Error: " + ex);
        }
        return null;
    }
}

