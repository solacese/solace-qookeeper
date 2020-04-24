package com.solace.dev;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderHashingTest {

    private Order makeOrder(long id) {
        return new Order(id, Order.Side.BUY, "GM", 22.50, 2250);
    }

    @Test
    public void basicOrderHashingTest() {
        final int maxHashCount = 101;
        final OrderHashingStrategy strategy = new OrderHashingStrategy(maxHashCount);
        // reverse( id % maxHashCount )
        assertEquals( "000", strategy.makeHash(makeOrder(   0)) );
        assertEquals( "100", strategy.makeHash(makeOrder(   1)) );
        assertEquals( "210", strategy.makeHash(makeOrder(  12)) );
        assertEquals( "220", strategy.makeHash(makeOrder( 123)) );
        assertEquals( "000", strategy.makeHash(makeOrder(1111)) );
    }

    @Test
    public void nullOrderTest() {
        final int maxHashCount = 128;
        final OrderHashingStrategy strategy = new OrderHashingStrategy(maxHashCount);
        assertEquals( "___", strategy.makeHash(null) );
    }

    @Test
    public void bucketingTest() {
        int maxHashCount = 9;
        OrderHashingStrategy strategy = new OrderHashingStrategy(maxHashCount);
        List<String> buckets = strategy.getBuckets();
        assertEquals( 9, buckets.size() );
    }
}
