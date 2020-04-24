package com.solace.qk.solace;

import com.solace.qk.ClientEventBusWrapper;
import com.solace.qk.Protocol;
import com.solacesystems.jcsmp.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolClientWrapper implements ClientEventBusWrapper {
    static final private Logger logger = LoggerFactory.getLogger(SolClientWrapper.class);

    public SolClientWrapper(SolSession solSession) {
        this.session = solSession.getSession();
        this.producer = solSession.getProducer();
        this.consumer = solSession.getConsumer();
    }

    public JSONObject sendJoinRequest(String group, String serviceAddress) {
        JSONObject request = Protocol.newJoinRequest(group, (String)session.getProperty(JCSMPProperties.CLIENT_NAME));
        logger.info("Sending join request: {}", request.toJSONString());
        try {
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setText(request.toJSONString());

            Requestor requestor = session.createRequestor();
            Topic topic = JCSMPFactory.onlyInstance().createTopic(serviceAddress);
            TextMessage response = (TextMessage)requestor.request(message, 10000, topic);
            if (response != null) {
                JSONObject result = (JSONObject) JSONValue.parse(response.getText());
                logger.info("RECEIVED join result: {}", result.toJSONString());
                return result;
            }
            else {
                logger.warn("NO result arrived for Join Request");
            }
        }
        catch(Exception ex) {
            logger.error("EXCEPTION sending join request");
            ex.printStackTrace();
        }
        return null;
    }

    public JSONObject sendLeaveRequest(String group, String queueName, String serviceAddress) {
        JSONObject request = Protocol.newLeaveRequest(group, (String)session.getProperty(JCSMPProperties.CLIENT_NAME), queueName);
        logger.info("Sending leave request: {}", request.toJSONString());
        try {
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setText(request.toJSONString());

            Requestor requestor = session.createRequestor();
            Topic topic = JCSMPFactory.onlyInstance().createTopic(serviceAddress);
            TextMessage response = (TextMessage)requestor.request(message, 10000, topic);
            if (response != null) {
                JSONObject result = (JSONObject) JSONValue.parse(response.getText());
                logger.info("RECEIVED leave result: {}", result.toJSONString());
                return result;
            }
            else {
                logger.warn("NO result arrived for LEAVE Request");
            }
        }
        catch(Exception ex) {
            logger.error("EXCEPTION sending LEAVE request");
            ex.printStackTrace();
        }
        return null;
    }

    final private JCSMPSession session;
    final private XMLMessageProducer producer;
    final private XMLMessageConsumer consumer;
}
