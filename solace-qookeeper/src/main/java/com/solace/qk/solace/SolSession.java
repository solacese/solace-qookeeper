package com.solace.qk.solace;

import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class SolSession {
    public SolSession(JCSMPSession session, XMLMessageProducer producer, XMLMessageConsumer consumer) {
        this.session = session;
        this.producer = producer;
        this.consumer = consumer;
    }

    public JCSMPSession getSession() {
        return session;
    }

    public XMLMessageProducer getProducer() {
        return producer;
    }

    public XMLMessageConsumer getConsumer() {
        return consumer;
    }

    final private JCSMPSession session;
    final private XMLMessageProducer producer;
    final private XMLMessageConsumer consumer;
}
