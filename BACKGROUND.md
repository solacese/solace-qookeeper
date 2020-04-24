# Data Parallel Load Balancing

Data-parallel processing partitions data via a `Key` in the data, 
so that records with one key always goes to the same place.

For sticky load-balancing to work there must be some amount of coordination and understanding 
between the producer and whatever manages the persistent streams or partitions of the input, 
as well as some from the consumer as well.

```text
            Manager
              |
              V
Producer -> Broker -> Consumer
```

## Participants 

I've packaged these requirements into libraries taking away almost all the responsibility, with 
configurations to control the behavior.

Let's start from the consumer and work our way backwards.
 
### The Consumer
- Must know what partition to connect and consume from
- Alternatively, the broker or manager could assign them a partition; in this case the consumer must 
know how to request a partition and connect to it

### The Manager
- Must know the number of discrete hash buckets
- Must know the number of partitions to distribute these buckets across
- Must understand the data hashing scheme and how to express the filters that attract all the events 
(and only the events) their hash
- Note, this is not the same as knowing to produce a hash for an event or a destination for an event. 
It must know *how to produce filters in the hashed key-values*.

### The Producer
- Must understand the data hashing scheme and how to produce messages with the hash.
- Must be able to serialize the topic for each message.
 

https://solace.com/blog/sticky-load-balancing-in-solace-pubsub-event-broker/ 
