package com.solace.qk;

import org.json.simple.JSONObject;

public interface ClientEventBusWrapper {

    JSONObject sendJoinRequest(String group, String serviceAddress);

    JSONObject sendLeaveRequest(String group, String queueName, String serviceAddress);

}
