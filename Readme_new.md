## StreamProcessingDemo

Kafka is a distributed, partitioned, replicated commit log service. It provides the functionality of a messaging system, but with a unique design. 

Kafka is used widely for stream processing and Kafka is really a good tools for stream processing. But Customers who use Kafka today struggle with monitoring / “seeing”/troubleshooting what is happening in their clusters.**Streams Messageing Manager(SMM)** cure Kafka blindness and help the  different streaming personas be more productive and provides an End-to-end integration with Ambari/Cloudera Manager, Grafana, Ranger & Atlas.

Kafka is a stream processing system, it also nees high availability and disaster recovery,but the legacy MirrorMaker has a lot of challenge/limitation, make it not easy to satisfy enterprise use.**Streams Replication Manager(SRM)** supports active-active, multi-cluster, cross DC replication & other complex scenarios and HA. it also integrates replication monitoring with SMM.


![width=800](StreamProcessing.jpg)

Because Kafka is used widely, we only show a simple Kafka producer and consumer and focus on SMM.
Because SMM and SRM is integrated in Cloudera Manager,we use the Cloudera Data Platform to do this workshop.

![width=800](ClouderaManager.jpg)

Check the Kafka and Streams Messaging Manager status is normal(Green)
# Running this 
### 1. The kafka services is running on the background. we create a topic with a single partition:
```
/opt/cloudera/parcels/CDH/bin/kafka-topics --create --bootstrap-server `hostname`:9092 --replication-factor 1 --partitions 1 --topic some-topic
```

### 2.Check the topic we just created:
```
/opt/cloudera/parcels/CDH/bin/kafka-topics  --list --bootstrap-server `hostname`:9092
```

### 3. Check the topic from SMM:
access SMM from SMM UI
![width=800](SMM_UI_Link.jpg)
or from the link http://grocery.vpc.cloudera.com:9991/ **grocery.vpc.cloudera.com need to be replaced**


CSP and CSM are focus on Kafka ecosystem, of course the most important one is Kafka.
This repo contains a simple demo for Kafka, it has a producer and a configurable number of consumers. It is intended for developers who what to know the internals of Kafka through code.

Spring is only used for dependency injection and Web, all the Kafka setup is done use purely the Kafka Client library.
