package com.solace.qk.solace;

import com.solace.qk.ServerEventBusWrapper;
import com.solace.qk.Protocol;
import com.solacesystems.jcsmp.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.solacesystems.jcsmp.JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS;
import static com.solacesystems.jcsmp.JCSMPSession.WAIT_FOR_CONFIRM;

public class SolServerWrapper implements ServerEventBusWrapper {
    static final private Logger logger = LoggerFactory.getLogger(SolServerWrapper.class);

    public SolServerWrapper(SolSession solSession) {
        this.session = solSession.getSession();
        this.producer = solSession.getProducer();
        this.consumer = solSession.getConsumer();
    }

    public void sendJoinResult(BytesXMLMessage request, String queueName, String clientName) throws Exception {
        JSONObject result = Protocol.newJoinResult(queueName, clientName);
        logger.info("QKPR:SND>> " + result.toJSONString());
        TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        message.setText(result.toJSONString());
        producer.sendReply(request, message);
    }

    public void sendLeaveResult(BytesXMLMessage request, String queueName, String clientName) throws Exception {
        JSONObject result = Protocol.newLeaveResult(queueName, clientName);
        logger.info("QKPR:SND>> " + result.toJSONString());
        TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        message.setText(result.toJSONString());
        producer.sendReply(request, message);
    }

    public boolean hookClientDisconnectEvent() {
        try {
            session.addSubscription(JCSMPFactory.onlyInstance().createTopic("#LOG/*/CLIENT/*/CLIENT_CLIENT_DISCONNECT/>"), true);
            return true;
        }
        catch (JCSMPException jex) {
            logger.error("Exception thrown adding subscription to #LOG/*/CLIENT/*/CLIENT_CLIENT_DISCONNECT/>", jex);
        }
        return false;
    }

    public Queue provisionQueue(String name) {
        Queue queue = JCSMPFactory.onlyInstance().createQueue(name);
        EndpointProperties properties = new EndpointProperties();
        try {
            session.provision(queue, properties, FLAG_IGNORE_ALREADY_EXISTS);
        }
        catch(JCSMPException jex) {
            jex.printStackTrace();
        }
        return queue;
    }

    public boolean subscribeQueue(Queue queue, Topic subscription, boolean waitForConfirm) {
        try {
            session.addSubscription(queue, subscription, getFlag(waitForConfirm));
            return true;
        }
        catch(JCSMPErrorResponseException jre) {
            // Swallow already-exists responses as they are expected
            if (jre.getSubcodeEx() == JCSMPErrorResponseSubcodeEx.SUBSCRIPTION_ALREADY_PRESENT)
                return true;
            jre.printStackTrace();
        }
        catch (JCSMPException jex) {
            jex.printStackTrace();
        }
        return false;
    }
    private static int getFlag(boolean waitForConfirm) {
        if (waitForConfirm) return WAIT_FOR_CONFIRM;
        return 0;
    }

    public FlowReceiver bindQueue(Queue queue) {
        try {
            ConsumerFlowProperties props = new ConsumerFlowProperties();
            props.setEndpoint(queue);
            props.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            FlowReceiver cons = session.createFlow(null, props, new EndpointProperties());
            cons.start();
            return cons;
        }
        catch(JCSMPException jex) {
            jex.printStackTrace();
        }
        return null;
    }

    public void sendStatusUpdate(JSONObject status, String svcStatusAddress) {
        try {
            logger.info("QKPR:STATUS:SND>> " + status.toJSONString());
            TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            message.setText(status.toJSONString());
            producer.send(message, JCSMPFactory.onlyInstance().createTopic(svcStatusAddress));
        }
        catch(JCSMPException jex) {
            logger.error("ERROR publishing status update", jex);
        }
    }
    final private JCSMPSession session;
    final private XMLMessageProducer producer;
    final private XMLMessageConsumer consumer;
}
