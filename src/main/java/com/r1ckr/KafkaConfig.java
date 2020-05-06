package com.r1ckr;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class KafkaConfig {

    Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;
    @Value("${kafka.topic.name}")
    private String topic;
    @Value("${kafka.producer.mode}")
    private String sync;
    @Value("${kafka.consumer.number}")
    int numConsumers = 3;
    @Value("${kafka.consumer.group.id}")
    private String consumerGroupId;

    // We are having just one consumer exposed with a controller to put X elements into the topic
    private DemoProducer demoProducer;

    // Consumers are async, so we are creating them in a list that we are gonna insert in a Thread Pool
    private List<DemoConsumer> demoConsumers = new ArrayList<>();

    // This is the Thread Pool we we are gonna insert the consumers
    private ExecutorService executorService = Executors.newFixedThreadPool(numConsumers);

    @Bean
    public DemoProducer demoProducer() throws ExecutionException, InterruptedException {
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", bootstrapServers);

        // This is mandatory, even though we don't send keys
        producerProps.put("key.serializer", StringSerializer.class.getName());
        producerProps.put("value.serializer", StringSerializer.class.getName());
        producerProps.put("acks", "1");

        // how many times to retry when produce request fails?
        producerProps.put("retries", "3");
        producerProps.put("linger.ms", 5);

        demoProducer = new DemoProducer(topic, producerProps, sync);
        demoProducer.start();
        System.out.println("Starting Producer...");
        demoProducer.produce("Starting Producer..."+System.currentTimeMillis());
        return demoProducer;
    }

    @Bean
    public List<DemoConsumer> demoConsumers() {
        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", consumerGroupId);
        consumerProps.put("key.deserializer", StringDeserializer.class.getName());
        consumerProps.put("value.deserializer", StringDeserializer.class.getName());

        for (int i = 0; i < numConsumers; i++) {
            DemoConsumer demoConsumer = new DemoConsumer(i, Arrays.asList(topic), consumerProps);
            demoConsumers.add(demoConsumer);
            executorService.submit(demoConsumer);
        }
        return demoConsumers;
    }

    @PreDestroy
    public void shutdownThis() throws ExecutionException, InterruptedException {
        // Shutdown producer
        System.out.println("Closing down Producer...");
        demoProducer.close();
        System.out.println("Producer closed!");

        // Shutdown consumers
        System.out.println("Shutting down consumers...");

        // We first call the shutdown method created in the consumer
        for (DemoConsumer consumer :
                demoConsumers) {
            consumer.shutdown();

        }

        // Then we call the shutdown on the executor service
        executorService.shutdown();

        System.out.println("Consumers stopped");
    }
}
