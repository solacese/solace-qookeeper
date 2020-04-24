# solace-qookeeper

A sample implementation of Sticky Load Balancing via Solace PubSub+
event brokers and Java APIs.  I wanted to do this to get a sense
of the requirements to achieve this. These requirements are wonderfully
documented and discussed by Mathew Hobbis in this blog:

https://solace.com/blog/sticky-load-balancing-in-solace-pubsub-event-broker/

See here [for more background discussion.](BACKGROUND.md)

# Demo

It's a standard maven project with two modules, if your maven is
working properly it should find all it's dependencies and build
successfully.

```bash
cd solace-qookeeper
mvn clean compile assembly:single # this builds a fat jar for easier demo
```

You can see it work on a Solace PubSub+ EventBroker by building
these projects and running the following components:

```cmd
java -jar target/solace-qookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar \
    src/main/resources/solconfig1.yml \
    src/main/resources/qkconfig1.yml
``` 

You should edit solconfig1.yml for your Solace PubSub+ connection; all configs are 
serialized using [snakeyaml](https://mvnrepository.com/artifact/org.yaml/snakeyaml).
```YML
!!com.solace.qk.solace.SolConfig
host: localhost
msgVpn: default
clientUsername: default
clientPassword: $ecret!
```

By default, the QKConfig items will be provisioned by the Qookeeper process 
at startup. If you want different configurations, you can change them.

## Sample Client

A sample client program is provided that tests the consumer group 
by repeatedly adding and removing consumers in the configured 
consumer group. You can run as many as you want concurrently to 
increase the load. It takes the same configuration Solace configuration 
as the server, and a much simpler client configuration:

```YML
!!com.solace.qk.QKClientConfig
groupName: cg1
serviceAddress: qk/service/cg1
```

```cmd
java -cp target/solace-qookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar \
    com.solace.qk.ClientChurnTest \
    src/main/resources/solconfig1.yml \
    src/main/resources/qkclientconfig1.yml
``` 

# Components

There are 2 direct components in this system, and one that is
provided by another library I've written in the past.

## Partition Manager

The `Qookeeper` is a separate process that joins the message bus
to manage the partitions and consumers consuming from them. It
creates a queue per each partition, calculates the range of hash
buckets and subscribes each queue to an equal subset of the hash
buckets.

The manager also binds to a service queue for inbound consumer
requests to Join the consumer group.  The manager tracks the number
of consumers on all the queues to distribute the consumers evenly
across the partitions. The manager tracks bind/unbind events in
case the consumers don't send requests.

## Consumer Client

If application owners are comfortable with managing the consumers,
then can do this by simply configuring them to bind to separate
queues to make sure all partitions are consumed evenly.

But if not, you can use the `CKClient` library to send a `JoinRequest`
to the `Qookeeper` which sends back a response with the name of the
queue for this application to bind to. This way, consumers are all
configured the same and are not configured with partitioning details.

## Producer Processes

For the paritioning strategy to work, the Producers must produce
events on hierarchical topics that *include the calculated hash*
for each message.

- *Hierarchical Topics* this is solved by using an abstract
topic-serializer strategy, just as you would employ a data-serialization
strategy 
- *Hash Calculator* like above, this is implemented using
an abstract hashing strategy instance, and a custom topic-serializer
that ties the calculated hash into the generated topics.

There are generalized implementations of both classes available in
my [topic-serialization](https://github.com/koverton/topic-serialization)
library.

