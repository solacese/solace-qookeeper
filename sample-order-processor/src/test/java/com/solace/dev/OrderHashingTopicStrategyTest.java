package com.solace.dev;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderHashingTopicStrategyTest {

    private Order makeOrder(long id) {
        return new Order(id, Order.Side.BUY, "GM", 22.50, 2250);
    }

    @Test
    public void basicOrderHashingTest() {
        final int maxHashCount = 100;
        final OrderHashingStrategy hashing = new OrderHashingStrategy(maxHashCount);
        final OrderHashingTopicStrategy topics = new OrderHashingTopicStrategy(hashing);
        assertEquals( "oms/order/00/GM/0", topics.makeTopic(makeOrder(0)).toString() );
        assertEquals( "oms/order/01/GM/10", topics.makeTopic(makeOrder(10)).toString() );
        assertEquals( "oms/order/00/GM/100", topics.makeTopic(makeOrder(100)).toString() );
    }

    @Test
    public void nullOrderTest() {
        final int maxHashCount = 128;
        final OrderHashingStrategy hashing = new OrderHashingStrategy(maxHashCount);
        final OrderHashingTopicStrategy topics = new OrderHashingTopicStrategy(hashing);
        assertEquals( "oms/order/___/_/_", topics.makeTopic(null).toString() );
    }
}
