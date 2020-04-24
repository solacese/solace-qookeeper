package com.solace.qk;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;
import com.solace.qk.Protocol;

import static org.junit.Assert.assertEquals;

public class MessageTest {

    @Test
    public void joinRequestTest() {
        JSONObject inreq = Protocol.newJoinRequest("g1", "c1");
        String input = inreq.toJSONString();
        System.out.println("REQUEST: " + input);

        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setText(input);

        JSONObject outreq = (JSONObject) JSONValue.parse(msg.getText());
        String output = outreq.toJSONString();
        System.out.println("OUTPUT: " + output);

        assertEquals(input, output);
    }

    @Test
    public void leaveRequestTest() {
        JSONObject inreq = Protocol.newLeaveRequest("g1", "c1", "q1");
        String input = inreq.toJSONString();
        System.out.println("REQUEST: " + input);

        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setText(input);

        JSONObject outreq = (JSONObject) JSONValue.parse(msg.getText());
        String output = outreq.toJSONString();
        System.out.println("OUTPUT: " + output);

        assertEquals(input, output);
    }
}
