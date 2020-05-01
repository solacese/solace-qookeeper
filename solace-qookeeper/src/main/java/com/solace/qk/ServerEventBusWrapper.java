package com.solace.qk;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.Topic;
import org.json.simple.JSONObject;

/**
 * Convenience wrapper around an existing EventBus connection that implements the server side of the QooKeeper protocol.
 */
public interface ServerEventBusWrapper {

    Queue provisionQueue(String name);

    boolean subscribeQueue(Queue queue, Topic subscription, boolean waitForConfirm);

    FlowReceiver bindQueue(Queue queue);

    void sendJoinResult(BytesXMLMessage request, String queueName, String clientName) throws Exception;

    void sendLeaveResult(BytesXMLMessage request, String queueName, String clientName) throws Exception;

    void sendStatusUpdate(JSONObject status, String svcStatusAddress);
}
