# Data Parallel Load Balancing

Data-parallel processing involves the partitioning of data via a
`Key` in the data, so that records with one key always go to the
same processor. That makes all updates on that `Key` "stick" to one
processor as opposed to being randomly distributed across all
available processors. This supports two important capabilities:

* *Stateful Processing*: by routing all updates on a `Key` to the
same processor, it can build up all state around that `Key` (typically
an Order or Product ID) across time and use that state to inform
event handling.

* *Ordered Processing*: even with stateless processors, if two
different events on the same `Key` are routed to different processors,
the second update might be completed before the first update,
breaking the order of updates. For example, it's kind of important
to handle order creation and cancellation events in that sequence,
and not the other way 'round.

For sticky load-balancing to work there must be some amount of
coordination and understanding between the producer and whatever
manages the persistent streams or partitions of the input, as well
as some from the consumer as well.

![QK Flow](resources/QK_Flow.png)

## Participants 

I've packaged these requirements into libraries taking away almost
all the responsibility, with configurations to control the behavior.

Let's start from the consumer and work our way backwards.

### The Consumer

Consumers should have the least responsibility in the system:

- Must know what partition to connect and consume from
- Alternatively, the broker or manager could assign them a partition;
in this case the consumer must know how to request a partition and
connect to it

### The Manager

Handles as many responsibilities as possible:

- Must know the number of discrete hash buckets
- Must know the number of partitions to distribute these buckets across
- Must understand the data hashing scheme and how to express the 
filters that attract all the events (and only the events) their hash
- Note, this is not the same as knowing to produce a hash for an event 
or a destination for an event.  It must know *how to produce filters in 
the hashed key-values*.

### The Producer

Ideally you could avoid this if you can guarantee all events natively 
have `Keys`, and that hashing and serialization were built into the 
libraries. Most of that time that's not possible, so the following 
capabilities are required in the publishers:

- Must understand the data hashing scheme and how to produce messages with the hash.
- Must be able to serialize the topic for each message respecting the hashing strategy.
 
## Detailed Flow

The more detailed end-to-end flow looks like this then:

![QK Flow](resources/QK_Flow_Annotated.png)

## References

https://solace.com/blog/sticky-load-balancing-in-solace-pubsub-event-broker/

https://solace.com/blog/topic-hierarchy-best-practices/


