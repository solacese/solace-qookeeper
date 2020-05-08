package com.solace.qk;

import com.solace.dev.ReflectionBasedHashingStrategy;
import com.solace.dev.ReflectionBasedHashingTopicStrategy;
import com.solace.dev.TopicStrategy;
import com.solace.qk.solace.SolConfig;
import com.solace.qk.solace.SolConnector;
import com.solacesystems.jcsmp.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class TestOrderProducer {

    final static String randInstr(Random random) {
        int targetStringLength = 5;
        return random.ints(65, 91)
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
    final static TestOrder.Side randSide(Random random) {
        if (random.nextBoolean())
            return TestOrder.Side.BUY;
        return TestOrder.Side.SELL;
    }
    final static TestOrder randomOrder(Random random, AtomicLong idgen) {
        return new TestOrder(
                idgen.incrementAndGet(),
                randSide(random),
                randInstr(random),
                random.nextDouble(),
                random.nextDouble());
    }

    static public void main(String[] args) {

        if (args.length < 2) {
            System.out.println("\n\n\tUSAGE: com.solace.qk.TestOrderProducer <solace-config-file.yml> <qk-config-file.yml>\n\n");
            System.exit(0);
        }

        try {
            SolConfig sconfig = SolConfig.fromFile(args[0]);
            QKConfig config = QKConfig.fromFile(args[1]);

            SolConnector connector = new SolConnector(sconfig);
            connector.connect();

            ReflectionBasedHashingStrategy hashingStrategy = new ReflectionBasedHashingStrategy(
                    config.getHashCount(),
                    config.getObjectIdFieldGetter());
            TopicStrategy<Object> topicStrategy = new ReflectionBasedHashingTopicStrategy(
                    Class.forName(config.getObjectClassName()),
                    config.getTopicDefinition(),
                    hashingStrategy);

            boolean processing = true;
            Random random = new Random();
            AtomicLong idgen = new AtomicLong(1000);
            XMLMessageProducer producer = connector.getSession().getProducer();
            while (processing) {
                TestOrder order = randomOrder(random, idgen);
                Topic topic = topicStrategy.makeTopic(order);
                TextMessage message = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
                message.setText(order.toString());
                System.out.println("Sending to topic " + topic.getName());
                producer.send(message, topic);
                Thread.sleep(1000);
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
