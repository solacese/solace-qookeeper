package com.solace.qk;

import com.solace.qk.QKClient;
import com.solace.qk.QKClientConfig;
import com.solace.qk.solace.SolClientWrapper;
import com.solace.qk.solace.SolConfig;
import com.solace.qk.solace.SolConnector;
import com.solacesystems.jcsmp.*;

import java.util.*;

/**
 * A system test that starts several QKCLient instances and cycle through them
 * joining and leaving the same Consumer Group. This provides a useful and
 * interesting client load for testing the QooKeeper model for load-balancing a
 * Consumer Group.
 */
class ClientChurnTest {
    static private class StubQueueListener implements XMLMessageListener {
        public void onReceive(BytesXMLMessage msg) { msg.ackMessage(); }
        public void onException(JCSMPException e) { }
    }

    static public void main(String[] args) {

        if (args.length < 2) {
            System.out.println("\n\n\tUSAGE: com.solace.qk.ClientChurnTest <solace-config-file.yml> <qk-client-config-file.yml>\n\n");
            System.exit(0);
        }

        try {
            SolConfig sconfig = SolConfig.fromFile(args[0]);
            QKClientConfig clientConfig = QKClientConfig.fromFile(args[1]);
            int queueCount = 8;

            SolConnector connector = new SolConnector(sconfig);
            connector.connect();
            SolClientWrapper clientConnection = new SolClientWrapper(connector.getSession());
            StubQueueListener listener = new StubQueueListener();

            QKClient client = new QKClient(clientConfig, clientConnection);

            // Loop repeatedly joining and leaving the Consumer Group
            Map<String, FlowReceiver> joinedQueues = new HashMap();
            while (true) {
                for (int i = 0; i < queueCount; i++) {
                    // Join all the available queues
                    String qname = client.join(clientConfig.getGroupName());
                    FlowReceiver binding = connector.bindQueue(qname, listener);
                    binding.start();
                    System.out.println("Joined Queue: " + qname);
                    joinedQueues.put(qname, binding);
                    Thread.sleep(1000);
                }
                for (Map.Entry<String, FlowReceiver> entry : joinedQueues.entrySet()) {
                    // Leaving all the joined queues
                    String qname = entry.getKey();
                    FlowReceiver binding = entry.getValue();
                    String left = client.leave(clientConfig.getGroupName(), qname);
                    binding.close();
                    System.out.println("Left Queue: " + left);
                    Thread.sleep(1000);
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
