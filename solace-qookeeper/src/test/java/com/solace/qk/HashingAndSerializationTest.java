package com.solace.qk;

import com.solace.dev.*;
import com.solacesystems.jcsmp.Topic;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class HashingAndSerializationTest {
    public class Thingy {
        public Thingy(long id) {
            this.id = id;
        }
        public long getID() {
            return id;
        }
        private long id;
    }

    private ReflectionBasedHashingStrategy hashingStrategy =
            new ReflectionBasedHashingStrategy(80, "getID");
    private TopicStrategy<Object> topicStrategy =
            new ReflectionBasedHashingTopicStrategy(
                    Thingy.class,
                    "{hash}/{id}",
                    hashingStrategy );

    @Test
    public void basicStrategyTopicTest() {
        Thingy t = new Thingy(5L);
        String hash = hashingStrategy.makeHash(t);
        assertEquals("50", hash);
        Topic topic = topicStrategy.makeTopic(t);
        assertEquals("50/5", topic.getName());
    }
}
