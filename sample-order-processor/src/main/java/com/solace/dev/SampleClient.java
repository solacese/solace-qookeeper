package com.solace.dev;

import com.solace.qk.QKClient;
import com.solace.qk.QKClientConfig;
import com.solace.qk.solace.SolClientWrapper;
import com.solace.qk.solace.SolConfig;
import com.solace.qk.solace.SolConnector;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.XMLMessageListener;

import java.util.HashMap;
import java.util.Map;

class SampleClient {
    static private class StubQueueListener implements XMLMessageListener {
        public void onReceive(BytesXMLMessage msg) {
            System.out.println("Received msg");
            msg.ackMessage();
        }
        public void onException(JCSMPException e) { }
    }

    static public void main(String[] args) throws Exception {
        SolConnector connector = new SolConnector(
                new SolConfig(
                        "localhost",
                        "default",
                        "default",
                        "")
        );
        connector.connect();
        SolClientWrapper clientConnection = new SolClientWrapper(connector.getSession());


        final QKClientConfig clientConfig = new QKClientConfig(
                "cgtest",
                "qk/service/cgtest");
        QKClient client = new QKClient(clientConfig, clientConnection);


        FlowReceiver consumer = connector.bindQueue(
                client.join(clientConfig.getGroupName()),
                new StubQueueListener());
        System.out.println("Joined Queue: " + consumer.getDestination().getName());
        consumer.start();


        while(true) {
            Thread.sleep(1000);
        }

    }
}
