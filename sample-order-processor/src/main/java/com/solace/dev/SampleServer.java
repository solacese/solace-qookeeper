package com.solace.dev;

import com.solace.qk.QKConfig;
import com.solace.qk.QooKeeper;
import com.solace.qk.solace.SolConfig;
import com.solace.qk.solace.SolConnector;
import com.solace.qk.solace.SolServerWrapper;

class SampleServer {
    static public void main(String[] args) {
        try {
            SolConnector connector = new SolConnector(
                    new SolConfig(
                            "localhost",
                            "default",
                            "default",
                            "")
            );
            connector.connect();
            SolServerWrapper session = new SolServerWrapper(connector.getSession());


            QKConfig serverConfig = new QKConfig(
                    "cgtest",
                    100,
                    8,
                    "cgtest/",
                    "qkservice/cgtest",
                    "qk/service/cgtest",
                    "qk/service/status",
                    "UNUSED",
                    "UNUSED",
                    "UNUSED");
            OrderHashingStrategy hashingStrategy =
                    new OrderHashingStrategy(serverConfig.getHashCount());
            OrderHashingTopicStrategy topicStrategy =
                    new OrderHashingTopicStrategy(hashingStrategy);


            QooKeeper qkeeper = new QooKeeper(serverConfig, session, hashingStrategy, topicStrategy);
            qkeeper.init();
            qkeeper.mainListeningLoop();


            while(true) {
                Thread.sleep(5000);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
