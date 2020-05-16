package com.solace.qk;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.TextMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Protocol {
    static final private Logger logger = LoggerFactory.getLogger(Protocol.class);

    static final public String CLIENTNAME = "client";
    static final public String GROUPNAME = "group";
    static final public String QUEUENAME = "queue";
    static final public String QUEUES = "queuelist";
    static final public String MSGTYPE = "type";

    // I know this size is unnecessary, but json-simple always opts for the larger types
    static final public long JOINREQUEST = 1;
    static final public long JOINRESULT = 11;
    static final public long LEAVEREQUEST = 2;
    static final public long LEAVERESULT = 12;
    static final public long GROUPINFO = 3;

    static public String msgTypeString(long msgtype) {
        if (msgtype == JOINREQUEST )
            return "JOINREQUEST";
        else if (msgtype == JOINRESULT )
            return "JOINRESULT";
        else if (msgtype == LEAVEREQUEST )
            return "LEAVEREQUEST";
        else if (msgtype == LEAVERESULT )
            return "LEAVERESULT";
        else if (msgtype ==  GROUPINFO )
            return "GROUPINFO";
        else
            return "?UNKNOWN?";
    }

    static public JSONObject parseMsg(BytesXMLMessage msg) {
        if (msg instanceof TextMessage) {
            // Parse and log the inbound message
            String text = ((TextMessage) msg).getText();
            logger.info("QKPR:RECV<< " + text);
            JSONObject request = (JSONObject) JSONValue.parse(text);
            if (request == null)
                logger.error("Could not parse inbound request: {}", text);
            return request;
        }
        return null;
    }

    static public JSONObject newJoinRequest(String groupName, String clientName) {
        JSONObject request = new JSONObject();
        request.put(MSGTYPE, JOINREQUEST);
        request.put(GROUPNAME, groupName);
        request.put(CLIENTNAME, clientName);
        return request;
    }
    static public JSONObject newJoinResult(String queueName, String clientName) {
        JSONObject request = new JSONObject();
        request.put(MSGTYPE, JOINRESULT);
        request.put(QUEUENAME, queueName);
        request.put(CLIENTNAME, clientName);
        return request;
    }

    static public JSONObject newLeaveRequest(String groupName, String clientName, String queueName) {
        JSONObject request = new JSONObject();
        request.put(MSGTYPE, LEAVEREQUEST);
        request.put(GROUPNAME, groupName);
        request.put(CLIENTNAME, clientName);
        request.put(QUEUENAME, queueName);
        return request;
    }
    static public JSONObject newLeaveResult(String queueName, String clientName) {
        JSONObject request = new JSONObject();
        request.put(MSGTYPE, LEAVERESULT);
        request.put(QUEUENAME, queueName);
        request.put(CLIENTNAME, clientName);
        return request;
    }

}
