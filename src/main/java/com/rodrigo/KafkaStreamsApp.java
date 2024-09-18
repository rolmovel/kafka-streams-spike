package com.rodrigo;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;

import java.util.Arrays;
import java.util.Properties;

public class KafkaStreamsApp {
    public static void main(String[] args) {
        // Configuraciones de Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-spike-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        // Crear un KStream desde un tópico
        KStream<String, String> textLines = builder.stream("input-topic");


        // Contar palabras usando un KTable
        KTable<String, Long> wordCounts = textLines
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split(" ")))
                .filter((key, word) -> !word.equals("token"))
                .groupBy((key, word) -> word)
                .count(Materialized.as("Counts"));
        wordCounts.toStream().foreach((key, value) -> System.out.println("Key = " + key + ", Value = " + value));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // Agregar un shutdown hook para cerrar la aplicación de streams de forma limpia
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
