package com.solace.qk;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple QooKeeper managed Consumer Group client. This client provides
 * a simplified API for sending Consumer Group requests to the svc-manager.
 * Supported requests are:
 *
 * - join: sends a join request and waits for the assigned queue name to be provided by the service.
 * - leave: sends a graceful leave request to the service identifying the queue being left. This is
 *   necessary for client processes that could have multiple queue consumers, so it's important to
 *   clarify the client+queue combination being removed.
 *
 * And that's it!
 */
public class QKClient {
    static final private Logger logger = LoggerFactory.getLogger(QKClient.class);

    public QKClient(QKClientConfig config, ClientEventBusWrapper session) {
        this.config = config;
        this.session = session;
    }

    /**
     * Send a join request over the Solace PubSub+ event bus, awaiting the resulting queue name.
     *
     * @param consumerGroup String identifying the Consumer Group being joined.
     * @return String queueName allocated to this client by the QooKeeper service mgr.
     */
    public String join(String consumerGroup) {
        JSONObject result = session.sendJoinRequest(consumerGroup, config.getServiceAddress());
        if (result != null) {
            logger.info("JoinRequest:RESULT: {}", result.toJSONString());
            return (String) result.get(Protocol.QUEUENAME);
        }
        return null;
    }

    /**
     * Send a Leave request over the Solace PubSub+ event bus, awaiting the confirmation of removal by the QooKeeper service mgr.
     *
     * @param consumerGroup String identifying the Consumer Group being left.
     * @return String queueName being left that was previously allocated to this client by the QooKeeper service mgr.
     */
    public String leave(String consumerGroup, String queueName) {
        JSONObject result = session.sendLeaveRequest(consumerGroup, queueName, config.getServiceAddress());
        if (result != null) {
            logger.info("LeaveRequest:RESULT: {}", result.toJSONString());
            return (String) result.get(Protocol.QUEUENAME);
        }
        return null;
    }

    final private ClientEventBusWrapper session;
    final private QKClientConfig config;
}
