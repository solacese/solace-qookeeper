package com.solace.qk.solace;

import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolConnector {
    static final private Logger logger = LoggerFactory.getLogger(SolConnector.class);

    public SolConnector(SolConfig config) throws InvalidPropertiesException {
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, config.getHost());
        properties.setProperty(JCSMPProperties.USERNAME, config.getClientUsername());
        properties.setProperty(JCSMPProperties.VPN_NAME,  config.getMsgVpn());
        properties.setProperty(JCSMPProperties.PASSWORD, config.getClientPassword());

        session = JCSMPFactory.onlyInstance().createSession(properties);
    }

    public SolSession getSession() {
        return new SolSession(session, producer, consumer);
    }

    public void setDirectListener(DirectMsgHandler listener) {
        this.directListener = listener;
    }

    public boolean connect() {
        try {
            session.connect();
            producer = session.getMessageProducer(
                    new JCSMPStreamingPublishCorrelatingEventHandler() {
                        public void handleError(String msgid, JCSMPException cause, long ts) {
                            logger.error("ERROR SENDING MSG: {}", msgid);
                            cause.printStackTrace();
                        }
                        public void responseReceived(String msgid) {
                            logger.info("Message {} ACKd.", msgid);
                        }
                        public void responseReceivedEx(Object key) {
                            logger.info("Message key:{} ACKd", key);
                        }
                        public void handleErrorEx(Object key, JCSMPException cause, long ts) {
                            logger.error("ERROR SENDING MSG-key:{}", key);
                            cause.printStackTrace();
                        }
                    },
                    new JCSMPProducerEventHandler() {
                        public void handleEvent(ProducerEventArgs args) {
                            logger.warn("PRODUCER EVENT: {}", args.getInfo());
                        }
                    }
            );
            consumer = session.getMessageConsumer(
                    new XMLMessageListener() {
                        public void onReceive(BytesXMLMessage message) {
                            if (directListener != null) {
                                directListener.onDirectMessage( message );
                            }
                        }
                        public void onException(JCSMPException cause) {
                            logger.error("CONSUMER EXCEPTION");
                            cause.printStackTrace();
                        }
                    }
            );
            consumer.start();
            return true;
        }
        catch(JCSMPException jex) {
            jex.printStackTrace();
        }
        return false;
    }

    public FlowReceiver bindQueue(String qname, XMLMessageListener msgHandler) {
        try {
            Queue queue = JCSMPFactory.onlyInstance().createQueue(qname);
            ConsumerFlowProperties props = new ConsumerFlowProperties();
            props.setEndpoint(queue);
            props.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
            FlowReceiver cons = session.createFlow(msgHandler, props, new EndpointProperties());
            cons.start();
            return cons;
        }
        catch(JCSMPException jex) {
            jex.printStackTrace();
        }
        return null;
    }

    final private JCSMPSession session;
    private XMLMessageProducer producer;
    private XMLMessageConsumer consumer;
    private DirectMsgHandler   directListener;
}
