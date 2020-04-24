package com.solace.qk;

import com.solace.dev.*;
import com.solace.qk.model.QKModel;
import com.solace.qk.solace.*;
import com.solacesystems.jcsmp.*;
import com.solacesystems.jcsmp.Queue;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.solace.qk.Protocol.*;

public class QooKeeper<Event> implements DirectMsgHandler {
    static final private Logger logger = LoggerFactory.getLogger(QooKeeper.class);

    public QooKeeper(QKConfig config, SolServerWrapper session, HashingStrategy<Event> hashingStrategy, TopicStrategy<Event> topicStrategy) {
        this.config = config;
        this.session = session;
        this.hashingStrategy = hashingStrategy;
        this.topicStrategy   = topicStrategy;
        // this is where we will track all clients
        this.model = new QKModel(config.getGroupName());
    }

    public void init() {
        List<String> qlist = initQueues();
        initSubscriptions(qlist);
        session.hookClientDisconnectEvent();
    }

    private List<String> initQueues() {
        // Provision our service queue if it doesn't already exist
        Queue svcqueue = session.provisionQueue(config.getServiceQueue());
        Topic svctopic = JCSMPFactory.onlyInstance().createTopic(config.getServiceTopic());
        session.subscribeQueue(svcqueue, svctopic, true);
        // Provision managed queues
        List<String> qlist = new ArrayList<>();
        for (int q = 0; q < config.getQueueCount(); ++q) {
            // Create the queue
            String queueName = config.getQueuePrefix() + q;
            Queue queue = session.provisionQueue(queueName);
            if (queue == null)
                logger.error("ERROR: failed to create queue {}", queueName);
            else
                qlist.add(queueName);
        }
        model.initQueues(qlist);
        return qlist;
    }

    private void initSubscriptions(List<String> qlist) {
        ListIterator<String> buckets = hashingStrategy.getBuckets().listIterator();
        ListIterator<String> queues  = qlist.listIterator();
        Map<String,String> filters = new HashMap<>();
        while(buckets.hasNext()) {
            if (!queues.hasNext()) queues = qlist.listIterator();
            String queueName = queues.next();
            Queue queue = JCSMPFactory.onlyInstance().createQueue(queueName);
            filters.put("hash", buckets.next());
            addSubscription(queue, filters);
        }
    }

    private void addSubscription(Queue queue, Map<String,String> filters) {
        Topic subscriptionTopic = topicStrategy.makeSubscription(filters);
        if (!session.subscribeQueue(queue, subscriptionTopic, true)) {
            logger.error("ERROR: failed to subscribe {} to topic {}",
                    queue.getName(), subscriptionTopic.getName());
        }
    }

    final private Pattern pattern = Pattern.compile(".+/CLIENT_CLIENT_DISCONNECT/[^/]+/(.+)");
    public void onDirectMessage(BytesXMLMessage message) {
        // Only use this for connect/disconnect events
        String topic = message.getDestination().getName();
        Matcher matcher = pattern.matcher(topic);
        if (matcher.matches()) {
            String client = matcher.group(1);
            logger.warn("TRYING TO REMOVE CLIENT: " + client);
            int count = model.removeClient(client);
            logger.warn("Removed {} queue bindings for {}", count, client);
        }
    }

    public void mainListeningLoop() {
        Queue queue = JCSMPFactory.onlyInstance().createQueue(config.getServiceQueue());
        FlowReceiver receiver = session.bindQueue(queue);
        try {
            while(true) {
                // wait until we receive a request message
                BytesXMLMessage reqmsg = receiver.receive();
                if (reqmsg instanceof TextMessage) {
                    // Parse and log the inbound message
                    String text = ((TextMessage)reqmsg).getText();
                    logger.info("QKPR:RECV<< " + text);
                    JSONObject request = (JSONObject) JSONValue.parse( text );
                    if (request == null) {
                        logger.error("Could not parse inbound request: {}", text);
                        continue;
                    }

                    // Handle JSON request objects
                    long msgtype = (Long)request.get(MSGTYPE);
                    if (msgtype == JOINREQUEST)
                        handleJoinRequest(request, reqmsg);
                    else if(msgtype == LEAVEREQUEST)
                        handleLeaveRequest(request, reqmsg);
                    else
                        logger.warn("Unknown msgtype {}", msgtype);

                    // Send out model update
                    session.sendStatusUpdate(model.getModel(), config.getServiceStatusTopic());
                    reqmsg.ackMessage();
                    continue;
                }
                logger.error("No valid request input");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleJoinRequest(JSONObject request, BytesXMLMessage reqmsg) throws Exception {
        // Choose the next queue for them
        String queueName = model.nextQueue();
        String clientName = (String)request.get(CLIENTNAME);
        logger.info("Assigning client [{}] to queue [{}]", clientName, queueName);
        model.addClient(queueName, clientName);
        // Send result
        session.sendJoinResult(reqmsg, queueName, clientName);
    }

    private void handleLeaveRequest(JSONObject request, BytesXMLMessage reqmsg) throws Exception {
        String clientName = (String)request.get(CLIENTNAME);
        String queueName = (String)request.get(QUEUENAME);
        if(!model.removeClient(queueName, clientName)) {
            logger.warn("Client thought they were leaving {" + queueName + "} but not removed");
        }
        // send leave result
        session.sendLeaveResult(reqmsg, queueName, clientName);
    }

    final private QKConfig config;
    final private HashingStrategy<Event> hashingStrategy;
    final private TopicStrategy<Event> topicStrategy;
    final private SolServerWrapper session;
    final private QKModel model;



    static public void main(String[] args) {

        if (args.length < 2) {
            System.out.println("\n\n\tUSAGE: com.solace.qk.QooKeeper <solace-config-file.yml> <qk-config-file.yml>\n\n");
            System.exit(0);
        }

        try {
            SolConfig sconfig = SolConfig.fromFile( args[0] );
            QKConfig  config  = QKConfig.fromFile( args[1] );

            SolConnector connector = new SolConnector(sconfig);
            connector.connect();
            SolServerWrapper session = new SolServerWrapper(connector.getSession());

            ReflectionBasedHashingStrategy hashingStrategy = new ReflectionBasedHashingStrategy(
                    config.getHashCount(),
                    config.getObjectIdFieldGetter());
            TopicStrategy<Object> topicStrategy = new ReflectionBasedHashingTopicStrategy(
                    Class.forName(config.getObjectClassName()),
                    config.getTopicDefinition(),
                    hashingStrategy);

            QooKeeper qkeeper = new QooKeeper(
                    config,
                    session,
                    hashingStrategy,
                    topicStrategy);
            connector.setDirectListener(qkeeper);

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
