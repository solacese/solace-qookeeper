package com.solace.qk.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

class SortableEntryList {
    final ArrayList<Map.Entry> orderedQueues = new ArrayList();
    void load(JSONObject queueConsumers) {
        for(Object o : queueConsumers.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            orderedQueues.add(e);
        }
    }
    void sort() {
        orderedQueues.sort(new Comparator<Map.Entry>() {
            @Override
            public int compare(Map.Entry o1, Map.Entry o2) {
                return ((JSONArray)o1.getValue()).size() - ((JSONArray)o2.getValue()).size();
            }
        });
    }
}