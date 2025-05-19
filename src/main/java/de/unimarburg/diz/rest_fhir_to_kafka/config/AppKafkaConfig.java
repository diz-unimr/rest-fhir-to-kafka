/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.Message;

@Configuration
public class AppKafkaConfig {

  private static final Logger log = LoggerFactory.getLogger(AppKafkaConfig.class);
  private final String bootstrapServers;

  public AppKafkaConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
    log.debug("created 'AppKafkaConfig'");
  }

  @Bean
  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    // See https://kafka.apache.org/documentation/#producerconfigs for more properties

    log.debug("init kafka producerConfigs");
    return props;
  }

  @Bean
  public ProducerFactory<String, Message<String>> producerFactory() {
    final DefaultKafkaProducerFactory<String, Message<String>> kafkaProducerFactory =
        new DefaultKafkaProducerFactory<>(producerConfigs());
    log.debug("created KAFKA ProducerFactory");
    return kafkaProducerFactory;
  }

  @Bean
  public KafkaTemplate<String, Message<String>> kafkaTemplate() {
    final KafkaTemplate<String, Message<String>> messageKafkaTemplate =
        new KafkaTemplate<>(producerFactory());
    log.debug("created KafkaTemplate");
    return messageKafkaTemplate;
  }
}
