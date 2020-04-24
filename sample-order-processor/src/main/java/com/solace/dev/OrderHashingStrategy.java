package com.solace.dev;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class OrderHashingStrategy implements HashingStrategy<Order> {

    public OrderHashingStrategy(int maxHashCount) {
        this.maxHashCount = maxHashCount;
        int places = (int) Math.ceil( Math.log10( maxHashCount) );
        this.partitionFormat = "%0" + places + "d";

        char[] chars = new char[places];
        Arrays.fill(chars, '_');
        this.nullFormat = new String(chars);

        this.buckets = new ArrayList<>();
        for(long l = 0; l < maxHashCount; ++l) {
            this.buckets.add( makeHash(l) );
        }
    }

    public List<String> getBuckets() {
        return buckets;
    }

    @Override
    public String makeHash(Order order) {
        if (order == null) return nullFormat;
        return makeHash(order.getId());
    }

    private String makeHash(long l) {
        String partition = String.format(partitionFormat, (l % maxHashCount));
        // Reversing the integer-based ID provides better load-balancing behavior
        // because it avoids bucketing consecutive values together.
        return reverse(partition);
    }

    private static String reverse(String input)
    {
        char[] cs = input.toCharArray();
        int n = cs.length;
        int mid = n / 2;
        for (int i = 0; i < mid; ++i) {
            char c = cs[i];
            cs[i] = cs[n-1-i];
            cs[n-1-i] = c;
        }
        return new String(cs);
    }

    final private int maxHashCount;
    final private String partitionFormat;
    final private String nullFormat;
    final private List<String> buckets;
}