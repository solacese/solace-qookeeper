package com.solace.qk.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ModelTest {

    @Test
    public void entryListSortTest() {
        int qcount = 11;
        Random random = new Random();

        JSONObject queueConsumers = new JSONObject();
        for(Integer i = 0; i < qcount; i++) {
            JSONArray queueClients = new JSONArray();
            queueConsumers.put(i.toString(), queueClients);
        }

        SortableEntryList sorter = new SortableEntryList();
        sorter.load(queueConsumers);

        // Randomize some data
        for(Object o : queueConsumers.entrySet()) {
            Map.Entry e = (Map.Entry) o;
            int clientsToAdd = random.nextInt(100);
            for( int c = 0; c < clientsToAdd; c++) {
                ((JSONArray)e.getValue()).add("client");
            }
        }

        System.out.println("\n\nPre-sort: ");
        for(Object o : sorter.orderedQueues) {
            Map.Entry e = (Map.Entry)o;
            System.out.println("QP: " + e.getKey() + " : " + ((JSONArray)e.getValue()).size());
        }

        sorter.sort();

        System.out.println("\n\nPost-sort: ");
        for(Map.Entry e : sorter.orderedQueues) {
            System.out.println("QP: " + e.getKey() + " : " + ((JSONArray)e.getValue()).size());
        }
    }

    @Test
    public void disconnectTest() {
        QKModel model = new QKModel("mygroup");
        List<String> qlist = Arrays.asList("q1","q2","q3","q4");
        model.initQueues(qlist);
        model.addClient("q1", "c4");
        model.addClient("q1", "c3");
        model.addClient("q2", "c2");
        model.addClient("q2", "c3");
        model.addClient("q3", "c1");
        model.addClient("q4", "c4");
        model.addClient("q4", "c5");

        assertEquals( "q3", model.nextQueue());

        int count = model.removeClient("c4");
        assertEquals(2, count);
        count = model.removeClient("c3");
        assertEquals(2, count);

        assertEquals( "q1", model.nextQueue());

    }
}
