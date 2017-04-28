package com.r1ckr;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Taking this as an example:
 * https://github.com/gwenshap/kafka-examples/blob/master/SimpleCounter/src/main/java/com/shapira/examples/producer/simplecounter/SimpleCounter.java
 */
public class DemoProducer {

    private String topic;
    private String sync;
    private Properties producerProps;
    private Producer<String, String> producer;

    public DemoProducer(String topic, Properties producerProps, String sync) {
        this.topic = topic;
        this.producerProps = producerProps;
        this.sync = sync;

    }

    public void start() {
        producer = new KafkaProducer<String, String>(producerProps);
    }

    public void produce(String value) throws ExecutionException, InterruptedException {
        if (sync.equals("sync"))
            produceSync(value);
        else if (sync.equals("async"))
            produceAsync(value);
        else throw new IllegalArgumentException("Expected sync or async, got " + sync);

    }

    public void close() {
        producer.close();
    }

    /* Produce a record and wait for server to reply. Throw an exception if something goes wrong */
    private void produceSync(String value) throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, value);
        producer.send(record).get();

    }

    /* Produce a record without waiting for server. This includes a callback that will print an error if something goes wrong */
    private void produceAsync(String value) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, value);
        producer.send(record, new DemoProducerCallback());
    }

    private class DemoProducerCallback implements Callback {

        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                System.out.println("Error producing to topic " + recordMetadata.topic());
                e.printStackTrace();
            }
        }
    }
}
