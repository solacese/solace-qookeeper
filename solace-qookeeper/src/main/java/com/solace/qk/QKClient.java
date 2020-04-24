package com.solace.qk;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QKClient {
    static final private Logger logger = LoggerFactory.getLogger(QKClient.class);

    public QKClient(QKClientConfig config, ClientEventBusWrapper session) {
        this.config = config;
        this.session = session;
    }

    public String join(String consumerGroup) {
        JSONObject result = session.sendJoinRequest(consumerGroup, config.getServiceAddress());
        if (result != null) {
            logger.info("JoinRequest:RESULT: {}", result.toJSONString());
            return (String) result.get(Protocol.QUEUENAME);
        }
        return null;
    }

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
