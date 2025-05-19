/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka.output;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class ToTopicProducer {
  private static final Logger log = LoggerFactory.getLogger(ToTopicProducer.class);
  private final KafkaTemplate<String, Message<String>> kafkaTemplate;
  private int msgCount = 0;

  @Autowired
  public ToTopicProducer(KafkaTemplate<String, Message<String>> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;

    log.debug("created 'ToTopicProducer'");
  }

  private String getNextKey() {
    return String.valueOf(msgCount++);
  }

  public boolean sendTopic(String data) {

    Map<String, Object> headermap = new HashMap<>();
    headermap.put(KafkaHeaders.TOPIC, "produce-to-topic");
    // KafkaHeaders.PARTITION
    final String currentKey = getNextKey();
    headermap.put(KafkaHeaders.KEY, currentKey);
    headermap.put(KafkaHeaders.TIMESTAMP, Date.from(Instant.now()).getTime());

    var msg = MessageBuilder.createMessage(data, new MessageHeaders(headermap));

    log.debug("sending data to kafka ... ");
    var future = kafkaTemplate.send(msg);
    future.whenComplete(
        (result, ex) -> {
          if (ex == null) {
            try {
              handleSuccess(currentKey, future.get().getRecordMetadata());
            } catch (InterruptedException e) {
              throw new RuntimeException(e);
            } catch (ExecutionException e) {
              throw new RuntimeException(e);
            }
          } else {
            handleFailure(msg, ex);
          }
        });

    return true;
  }

  private void handleSuccess(String key, RecordMetadata metaData) {
    log.debug(
        "data with key '%s', offset '%s' has been persisted in kafka topic '%s'."
            .formatted(key, metaData.offset(), metaData.topic()));
  }

  private void handleFailure(Message<String> msg, Throwable exception) {
    log.error(
        "data with key '%s', failed to be stored in Kafka topic '%s'"
            .formatted(
                msg.getHeaders().get(KafkaHeaders.KEY), msg.getHeaders().get(KafkaHeaders.TOPIC)),
        exception);
  }
}
