package com.solace.qk;

import com.solace.dev.*;
import com.solace.qk.model.QKModel;
import com.solace.qk.model.RequestProcessor;
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

/**
 * QooKeeper is a Consumer Group service manager for event flows on a specific, expected Event type.
 * The requirement for this Event type knowledge is to understand how to serialize the topics on
 * which these events are published, and to understand the hashing strategy of those topics to
 * ensure balanced allocation of the domain of keys from these events.
 *
 * The QooKeeper ensures all the queues for this Consumer Group are provisioned and the Topic Space
 * is evenly distributed across these queues. It also binds to an inbound service queue listening
 * for QKClient requests to join or leave the consumer group. The QKClient object provides a simple
 * interface for consumers to send requests to join the Consumer Group and let the QooKeeper
 * simply send them the name of the queue to consume from. See also the QKClient object.
 *
 * @param <Event> Event type for which this Consumer Group is load-balancing consumers. The event
 *               type is required on the HashingStrategy and TopicStrategy passed into this object.
 */
public class QooKeeper<Event> implements DirectMsgHandler {
    static final private Logger logger = LoggerFactory.getLogger(QooKeeper.class);

    /**
     * Creates a QK instance for load-balancing a consumer group. Note, this does not Initialize the Consumer Group. See @init().
     *
     * @param config Configuration object for this QK instance.
     * @param session an existing Solace PubSub+ session, wrapped in an accessor object encapsulating events consumed and emitted by this object.
     * @param hashingStrategy
     * @param topicStrategy
     */
    public QooKeeper(QKConfig config, SolServerWrapper session, HashingStrategy<Event> hashingStrategy, TopicStrategy<Event> topicStrategy) {
        this.config = config;
        this.session = session;
        this.hashingStrategy = hashingStrategy;
        this.topicStrategy   = topicStrategy;
        // this is where we will track all clients
        this.model = new QKModel(config.getGroupName());
        this.processor = new RequestProcessor(session, model, config.getServiceStatusTopic());
    }

    /**
     * Initializes the consumer group infrastructure by: allocating all the expected queues
     * and subscribing these queues to properly hashed topic-subscriptions that evenly
     * distribute the keyspace of the Event flow across these queues.
     */
    public void init() {
        List<String> qlist = initQueues();
        initSubscriptions(qlist);
        session.hookClientDisconnectEvent();
    }

    /**
     * Provisions the underlying set of queues for this Consumer Group. If queues already
     * exist any exceptions are ignored.
     *
     * @return List of String queue names that are provisioned for this Consumer Group.
     */
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

    /**
     * Ensures a properly-hashed set of topic-subscriptions are added across the
     * queue list to evenly distribute the Event topicspace across the queues.
     *
     * @param qlist List of queue names to be subscribed to the hashed topic-subscriptions.
     */
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

    /**
     * A subscription is expressed as a set of field-based filters in the form:
     *  'field-name 1' : 'a*'
     *  'field-name 2' : 'NYSE001',
     *  ...
     * This subscription is hashed and serialized to a topic-subscription string
     * and added to this queue to attract Events matching this subscription.
     *
     * @param queue queue instance to be subscribed to the provided filter-expression.
     * @param filters list of field-based filters to be added to this queues subscriptions.
     */
    private void addSubscription(Queue queue, Map<String,String> filters) {
        Topic subscriptionTopic = topicStrategy.makeSubscription(filters);
        if (!session.subscribeQueue(queue, subscriptionTopic, true)) {
            logger.error("ERROR: failed to subscribe {} to topic {}",
                    queue.getName(), subscriptionTopic.getName());
        }
    }

    /**
     * Listen for disconnect events to remove them from allocated queues
     */
    final private Pattern pattern = Pattern.compile(".+/CLIENT_CLIENT_DISCONNECT/[^/]+/(.+)");
    @Override
    public void onDirectMessage(BytesXMLMessage message) {
        // Only using this for disconnect events (for now)
        String topic = message.getDestination().getName();
        Matcher matcher = pattern.matcher(topic);
        if (matcher.matches()) {
            String client = matcher.group(1);
            JSONObject leaveAllRequest = newLeaveRequest(config.getGroupName(), client, "*");
            processor.onRequest(leaveAllRequest, null);
        }
    }

    /**
     * Infinite loop to be run AFTER initialization of the QK instance is completed.
     * This loop binds to a service queue for Consumer Group service requests
     * (JOIN and LEAVE requests), handling them, updating the model and responding.
     */
    public void mainListeningLoop() {
        Queue queue = JCSMPFactory.onlyInstance().createQueue(config.getServiceQueue());
        FlowReceiver receiver = session.bindQueue(queue);
        try {
            while(true) {
                // wait until we receive a request message
                BytesXMLMessage reqmsg = receiver.receive();
                JSONObject request = parseMsg(reqmsg);
                if (request == null)
                    logger.error("No valid request input");
                else {
                    processor.onRequest(request, reqmsg);
                    reqmsg.ackMessage();
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    final private QKConfig config;
    final private HashingStrategy<Event> hashingStrategy;
    final private TopicStrategy<Event> topicStrategy;
    final private SolServerWrapper session;
    final private QKModel model;
    final private RequestProcessor processor;


    /**
     * QOOKEEPER ENTRYPOINT
     *
     * @param args USAGE: <solace-config-file.yml> <qk-config-file.yml>
     */
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
