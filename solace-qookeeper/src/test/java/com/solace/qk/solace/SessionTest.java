package com.solace.qk.solace;

import com.solacesystems.jcsmp.*;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionTest {
    private static final SolConfig config = new SolConfig(
            "localhost",
            "default",
            "default",
            "");

    @Test
    public void testConnect() throws InvalidPropertiesException {
        SolConnector session = new SolConnector(config);
        session.connect();
        try { Thread.sleep(1000); } catch(InterruptedException ex) {}
    }

    @Test
    public void testCreateQueue() throws InvalidPropertiesException {
        SolConnector session = new SolConnector(config);
        session.connect();
        SolServerWrapper serverSession = new SolServerWrapper(session.getSession());
        serverSession.provisionQueue("cgtest/01");
        try { Thread.sleep(1000); } catch(InterruptedException ex) {}
    }

    @Test
    public void testMsgTypes() {
        BytesXMLMessage bmsg = null;
        if (bmsg instanceof TextMessage)
            System.out.println("What the hell?");

        bmsg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        if (bmsg instanceof TextMessage)
            System.out.println("Good.");

    }

    @Test
    public void parseDisconnectTest() {
        String disconnect = "#LOG/INFO/CLIENT/eventmesh/CLIENT_CLIENT_DISCONNECT/default/kenoverton.nyc.rr.com/95196/#00000001/f7CAgzOzgq";
        Pattern pattern = Pattern.compile(".+/CLIENT_CLIENT_DISCONNECT/[^/]+/(.+)");
        Matcher matcher = pattern.matcher(disconnect);
        if (matcher.matches()) {
            System.out.println("MATCHED: " + matcher.group(1));
        }
        else
            System.out.println("NOOOOOOOO");
    }

    //@Test
    public void parseUnbindTest() throws Exception{
        String unbind = "2020-04-20T17:01:26.826+00:00 <local3.notice> eventmesh event: CLIENT: CLIENT_CLIENT_UNBIND: default kenoverton.nyc.rr.com/99130/#00140001/iB37Obw13B Client (97) kenoverton.nyc.rr.com/99130/#00140001/iB37Obw13B username default Unbind to Flow Id (425), ForwardingMode(StoreAndForward), final statistics - flow(0, 0, 0, 0, 0, 1, 0, 0, 0), isActive(No), Reason(Client issued unbind)";
        String topic = "#LOG/NOTICE/CLIENT/eventmesh/CLIENT_CLIENT_UNBIND/default/kenoverton.nyc.rr.com/99301/#00140001/nAKNDun2VJ";
        SolConnector connector = new SolConnector(config);
        connector.connect();
        connector.setDirectListener(
                new DirectMsgHandler() {
                    @Override
                    public void onDirectMessage(BytesXMLMessage message) {
                        byte[] bytes = message.getBytes();
                        System.out.println("BXML.getBytes: " + bytes.length);
                        if (message instanceof BytesMessage) {
                            byte[] data = ((BytesMessage)message).getData();
                            String text = new String(data, StandardCharsets.UTF_8);
                            System.out.println("DATASZ: "+data.length + "TXT:"+text);
                        }
                        else if (message instanceof  TextMessage) {
                            String data = ((TextMessage)message).getText();
                            System.out.println("MSG: " + data);
                        }
                        else {
                            System.out.println("MSG: " + message.dump());
                        }
                    }
                }
        );
        SolServerWrapper session = new SolServerWrapper(connector.getSession());
        session.hookClientDisconnectEvent();
        while(true) {
            Thread.sleep(1000);
        }
    }
}
