package com.solace.qk;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ConfigTest {

    @Test
    public void qkconfigTest() {
        QKConfig config = QKConfig.fromFile("src/test/resources/qkconfig1.yml");
        assertNotNull(config);
    }

    @Test
    public void qkclientConfigTest() {
        QKClientConfig config = QKClientConfig.fromFile("src/test/resources/qkclientconfig1.yml");
        assertNotNull(config);
    }
}
