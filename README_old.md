# Kafka Demo

This repo contains a simple demo for Kafka, it has a producer and a configurable number of consumers. It is intended for developers who what to know the internals of Kafka through code.

Spring is only used for dependency injection and Web, all the Kafka setup is done use purely the Kafka Client library.

## Running this:

#### 1. Download and run [Kafka](https://kafka.apache.org/downloads):
First run a Kafka instance locally:
```
$ wget http://mirrors.whoishostingthis.com/apache/kafka/0.10.2.1/kafka_2.12-0.10.2.1.tgz
$ tar -xvf kafka_2.12-0.10.2.1.tgz
$ cd kafka_2.12-0.10.2.1

## Start the embeded Zookeeper inside of the Kafka package
$ bin/zookeeper-server-start.sh config/zookeeper.properties
...
INFO binding to port 0.0.0.0/0.0.0.0:2181

## Start Kafka
$ bin/kafka-server-start.sh config/server.properties
...
INFO New leader is 0 (kafka.server.ZookeeperLeaderElector$LeaderChangeListener)
```
Note: It will help you to have the terminal where Kafka is running opened to see what it is doing while the producer and consumer do their work

#### 2. In the same Kafka directory create a topic with a single partition:
```
$ bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic some-topic
Created topic "some-topic".
```

#### 3. Compile our app:
```
$ mvn package
```

#### 4. Run the App with one producer and one consumer:
```
$ java -jar target/kafka-demo-0.0.1-SNAPSHOT.jar
```

#### 5. Produce 30 records and check the console:
```
$ curl -i http://localhost:8080/demo/produce
```
**Console output:**  
```
...
0: {partition=0, offset=34, value=19}
0: {partition=0, offset=35, value=20}
0: {partition=0, offset=36, value=21}
...
```
The output shows:  
```
<consumer ID>: {partition=<Partition number>, offset=<offset>, value=<value in that record>}
```

Here you can that we have only one consumer with id 0, consuming from the same partition (0) and updating the offset every time it reads a value

#### 6. Back in the Kafka directory, let's modify our topic to have 3 partitions:
```
$ bin/kafka-topics.sh --alter --zookeeper localhost:2181  --partitions 3 --topic some-topic
WARNING: If partitions are increased for a topic that has a key, the partition logic or ordering of the messages will be affected
Adding partitions succeeded!
```

#### 7. Run the app again and produce some records to see how Kafka load balances the calls of our consumer to the 3 partitions
We need to produce more records because we are using the same consumer group and Kafka gives back the same offset as before to the clients, if we change the consumer group then we will be again in offset 0 and read all the messages again.

#### 8. Let's now increase the number of consumers to 3, stop the app and run it with the `kafka.consumer.number` parameter:
```
$ java -jar -Dkafka.consumer.number=3 target/kafka-demo-0.0.1-SNAPSHOT.jar
```

#### 9. Produce 60 records and see how kafka assigns a partition to each consumer:
```
$ curl -i http://localhost:8080/demo/produce?count=60
```

Back in the app terminal check the output:
```
0: {partition=0, offset=50, value=54}
2: {partition=2, offset=18, value=55}
1: {partition=1, offset=19, value=56}
0: {partition=0, offset=51, value=57}
2: {partition=2, offset=19, value=58}
1: {partition=1, offset=20, value=59}
```

Note how each consumer is taking data only from 1 partition. Also check how the offset in the partition 0 is 30 times bigger than the rest of the partitions, this is because we had that partition previously.

#### 8. Let's now increase the number of consumers to 4 and see what happens:
```
$ java -jar -Dkafka.consumer.number=4 target/kafka-demo-0.0.1-SNAPSHOT.jar
```

#### 9. Produce 60 records:
```
$ curl -i http://localhost:8080/demo/produce?count=60
```

Back in the app terminal check the output:
```
0: {partition=0, offset=70, value=53}
2: {partition=2, offset=38, value=54}
1: {partition=1, offset=39, value=55}
0: {partition=0, offset=71, value=56}
2: {partition=2, offset=39, value=57}
1: {partition=1, offset=40, value=58}
0: {partition=0, offset=72, value=59}
```

See how one of the consumers isn't doing anything (consumer with id 3 in my case), this is because Kafka ensures that 
per consumer group there can be multiple partitions assigned to a consumer, but never multiple consumers in one partition. 
When using Kafka for purely for messaging is good to have a big number of partitions so we can scale up or down without 
worrying too much on altering our topics. 

