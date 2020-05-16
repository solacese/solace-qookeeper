package com.solace.qk.model;

import com.solace.qk.Protocol;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Structure:
 * {
 *     MSGTYPE : [Join Request, Leave Request, Group Info ],
 *     GROUPNAME : String group name,
 *     QUEUES : {
 *         "Queue 1" : [ list, of, unique, clients ],
 *         "Queue 2" : [ client, list ],
 *         ...
 *         "Queue N" : []
 *     }
 * }
 */
public class QKModel {
    static final private Logger logger = LoggerFactory.getLogger(QKModel.class);

    public QKModel(String groupName) {
        this.model = new JSONObject();
        this.queueConsumers = new JSONObject();
        // Type of the GroupInfo object
        this.model.put(Protocol.MSGTYPE, Protocol.GROUPINFO);
        // String group name
        this.model.put(Protocol.GROUPNAME, groupName);
        // String qname -> [client, list]
        this.model.put(Protocol.QUEUES, this.queueConsumers);
        // Queue sort function; sorts from smallest clientlist to largest
        // This allows fast allocation of new clients to the queue with
        // lowest consumer count
        this.queueList = new SortableEntryList();
    }

    /**
     * The list of queues and per-queue list of clients are initialized once.
     * For that to change would require a full rebalance, a significant operation.
     *
     * @param queues list of queue names (Strings) to be created in the model
     */
    public void initQueues(List<String> queues) {
        for(String queueName : queues) {
            if (!queueConsumers.containsKey(queueName)) {
                JSONArray queueClients = new JSONArray();
                queueConsumers.put(queueName, queueClients);
            }
        }
        queueList.load( queueConsumers );
    }

    /**
     * Retrieves the name of the queue with lowest number of allocated consumers.
     *
     * @return String queue name of the queue with the least allocated consumers.
     */
    public String nextQueue() {
        if (queueList.orderedQueues.size() == 0) return null;
        logger.info("Getting next queue for new joiner; qcount="+ queueList.orderedQueues.size());
        String key = (String) queueList.orderedQueues.get(0).getKey();
        logger.info("Getting next queue for new joiner; qcount="+ queueList.orderedQueues.size());
        return key;
    }

    /**
     * Add a named consumer client to a named queue in the model.
     *
     * @param queueName String queue name the consumer client is being added to.
     * @param client String unique consumer client ID being assigned to the queue.
     */
    public void addClient(String queueName, String client) {
        logger.info("Adding " + client + " to " + queueName);
        JSONArray clients = (JSONArray) queueConsumers.get(queueName);
        clients.add(client);
        queueList.sort();
        logger.info(queueList.toString());
    }

    /**
     * Remove a named consumer client from *all queues it's consuming in the group*.
     * This can be multiple queues. It is better behavior for the client to send
     * a LeaveRequest to gracefully leave the queue and re-sort our queue list,
     * but in the event clients disconnect non-gracefully this event needs to be
     * handled.
     *
     * @param client String unique consumer client ID being removed from queues.
     */
    public int removeClient(String client) {
        int count = 0;
        // Have to go through all queues removing this client
        for(Object o : queueConsumers.entrySet()) {
            Map.Entry entry = (Map.Entry)o;
            String queue = (String) entry.getKey();
            JSONArray clients = (JSONArray) entry.getValue();
                while (clients.remove(client)) {
                logger.info("Removing " + client + " from queue " + queue);
                ++count;
            }
        }
        // Re-order our list of queues based on the number of clients
        if (count > 0) queueList.sort();
        logger.info(queueList.toString());
        return count;
    }

    /**
     * Remove a named consumer client from the queue it's consuming in the group.
     * This is invoked when a client sends a LeaveRequest to gracefully leave the
     * queue and re-sort our queue list, but in the event clients disconnect
     * non-gracefully this event needs to be handled.
     *
     * @param queueName String queue-name to search for this client.
     * @param client String unique consumer client ID being removed from queue.
     * @return
     */
    public boolean removeClient(String queueName, String client) {
        logger.info("Removing " + client + " from " + queueName);
        JSONArray clients = (JSONArray) queueConsumers.get(queueName);
        boolean success = clients.remove(client);
        if (success)
            queueList.sort();
        else {
            logger.warn("Failed to remove {} from {} => {}", client, queueName, clients.toJSONString());
        }
        logger.info(queueList.toString());
        return success;
    }

    /**
     * Retrieve the current working model of queue->[client,list] mappings.
     * This is used by the QooKeeper to periodically publish state events
     * back to the Event Bus to make this state reproducible and subject
     * to analysis.
     *
     * @return Representing the full state of this model in a JSON object.
     */
    public JSONObject getModel() { return model; }

    final private JSONObject model;
    final private JSONObject queueConsumers;
    final private SortableEntryList queueList;
}
