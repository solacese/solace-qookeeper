package com.solace.qk;

import org.json.simple.JSONObject;

public class Protocol {
    static final public String CLIENTNAME = "client";
    static final public String GROUPNAME = "group";
    static final public String QUEUENAME = "queue";
    static final public String QUEUES = "queuelist";
    static final public String MSGTYPE = "type";

    static final public int JOINREQUEST = 1;
    static final public int JOINRESULT = 11;
    static final public int LEAVEREQUEST = 2;
    static final public int LEAVERESULT = 12;
    static final public int GROUPINFO = 3;

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
