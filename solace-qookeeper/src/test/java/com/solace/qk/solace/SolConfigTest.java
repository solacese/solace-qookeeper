package com.solace.qk.solace;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SolConfigTest {

    @Test
    public void solConfigTest() {
        SolConfig config = SolConfig.fromFile("src/test/resources/solconfig1.yml");
        System.out.println(config);
        assertNotNull(config);
    }
}
