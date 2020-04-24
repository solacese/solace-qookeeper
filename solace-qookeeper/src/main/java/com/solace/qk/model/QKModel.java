package com.solace.qk.model;

import com.solace.qk.Protocol;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class QKModel {
    static final private Logger logger = LoggerFactory.getLogger(QKModel.class);

    public QKModel(String groupName) {
        this.model = new JSONObject();
        this.queueConsumers = new JSONObject();
        this.model.put(Protocol.MSGTYPE, Protocol.GROUPINFO);
        this.model.put(Protocol.GROUPNAME, groupName);
        this.model.put(Protocol.QUEUES, this.queueConsumers);
        this.queueList = new SortableEntryList();
    }

    public void initQueues(List<String> queues) {
        for(String queueName : queues) {
            if (!queueConsumers.containsKey(queueName)) {
                JSONArray queueClients = new JSONArray();
                queueConsumers.put(queueName, queueClients);
            }
        }
        queueList.load( queueConsumers );
    }

    public String nextQueue() {
        if (queueList.orderedQueues.size() == 0) return null;
        logger.info("Getting next queue for new joiner; qcount="+ queueList.orderedQueues.size());
        String key = (String) queueList.orderedQueues.get(0).getKey();
        logger.info("Getting next queue for new joiner; qcount="+ queueList.orderedQueues.size());
        return key;
    }

    public void addClient(String queueName, String client) {
        logger.info("Adding " + client + " to " + queueName);
        JSONArray clients = (JSONArray) queueConsumers.get(queueName);
        clients.add(client);
        queueList.sort();
    }

    public int removeClient(String client) {
        int count = 0;
        // Have to go through all queues removing this client
        for(Object o : queueConsumers.entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            String queue = (String) entry.getKey();
            JSONArray clients = (JSONArray) entry.getValue();
            if (clients.remove(client)) {
                logger.info("Removing " + client + " from queue " + queue);
                ++count;
            }
        }
        if (count > 0) queueList.sort();
        return count;
    }

    public boolean removeClient(String queueName, String client) {
        logger.info("Removing " + client + " from " + queueName);
        JSONArray clients = (JSONArray) queueConsumers.get(queueName);
        boolean success = clients.remove(client);
        if (success)
            queueList.sort();
        return success;
    }

    public JSONObject getModel() { return model; }

    final private JSONObject model;
    final private JSONObject queueConsumers;
    final private SortableEntryList queueList;
}
