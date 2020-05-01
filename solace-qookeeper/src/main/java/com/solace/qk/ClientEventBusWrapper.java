package com.solace.qk;

import org.json.simple.JSONObject;

/**
 * Convenience wrapper around an existing EventBus connection that implements the client side of the QooKeeper protocol.
 */
public interface ClientEventBusWrapper {

    JSONObject sendJoinRequest(String group, String serviceAddress);

    JSONObject sendLeaveRequest(String group, String queueName, String serviceAddress);

}
