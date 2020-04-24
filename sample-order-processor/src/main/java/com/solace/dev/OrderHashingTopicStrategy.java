package com.solace.dev;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.Topic;

import java.util.Map;

class OrderHashingTopicStrategy implements TopicStrategy<Order> {
    private static final String prototype = "oms/order/{hash}/{instrument}/{id}";

    public OrderHashingTopicStrategy(OrderHashingStrategy hashingStrategy) {
        this.hashingStrategy = hashingStrategy;
    }

    @Override
    public String getTopicDefinition() {
        return prototype;
    }

    @Override
    public Topic makeTopic(Order order) {
        if (order == null)
            return JCSMPFactory.onlyInstance().createTopic(
                    String.format("oms/order/%s/_/_", hashingStrategy.makeHash(order))
            );
        return JCSMPFactory.onlyInstance().createTopic(
                String.format("oms/order/%s/%s/%d",
                        hashingStrategy.makeHash(order),
                        (order.getInstrument()==null) ? "_" : order.getInstrument(),
                        order.getId()
                ));
    }

    @Override
    public Topic makeSubscription(Map<String, String> keyBasedFilters) {
        if (keyBasedFilters.size() == 0)
            return JCSMPFactory.onlyInstance().createTopic("oms/order/>");
        return JCSMPFactory.onlyInstance().createTopic(
                "oms/order/" +
                        valueOrSplat(keyBasedFilters, "hash")   + '/' +
                        valueOrSplat(keyBasedFilters, "instrument") + '/' +
                        valueOrSplat(keyBasedFilters, "id")
        );
    }
    private String valueOrSplat(Map<String,String> map, String field) {
        if (map.containsKey(field)) return map.get(field);
        return "*";
    }

    private OrderHashingStrategy hashingStrategy;
}
