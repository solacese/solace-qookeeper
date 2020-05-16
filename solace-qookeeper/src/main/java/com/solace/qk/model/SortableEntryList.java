package com.solace.qk.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

/**
 * Sorts the list of entries based on the size of the client-list array.
 * Sorting is from smallest list to largest; when the model is asked to
 * add a new client to the consumer group, it's important to assign them
 * to queues with the lowest consumer-count to ensure balanced allocation
 * across all the queues in the group.
 */
class SortableEntryList {
    final ArrayList<Map.Entry> orderedQueues = new ArrayList();

    /**
     * Loads a JSON object in the following expected format:
     * {
     *     "queue name 1" : [ list, of, clients ],
     *     "queue name 2" : [ list, of, clients ],
     *     ...
     * }
     *
     * @param queueConsumers JSON map of queue-name to JSON list of assigned clients.
     */
    void load(JSONObject queueConsumers) {
        for(Object o : queueConsumers.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            orderedQueues.add(e);
        }
    }

    /**
     * Sorts the list of entries based on the size of the client-list array.
     * Sorting is from smallest list to largest; when the model is asked to
     * add a new client to the consumer group, it's important to assign them
     * to queues with the lowest consumer-count to ensure balanced allocation
     * across all the queues in the group.
     */
    void sort() {
        orderedQueues.sort(new Comparator<Map.Entry>() {
            @Override
            public int compare(Map.Entry o1, Map.Entry o2) {
                return ((JSONArray)o1.getValue()).size() - ((JSONArray)o2.getValue()).size();
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder();
        bldr.append("queues\n{\n");
        for(Map.Entry e : orderedQueues) {
            bldr.append("\t")
                    .append((e.getKey()))
                    .append(" : ")
                    .append(((JSONArray)e.getValue()).size())
                    .append(",\n");
        }
        bldr.append("}\n");
        return bldr.toString();
    }
}