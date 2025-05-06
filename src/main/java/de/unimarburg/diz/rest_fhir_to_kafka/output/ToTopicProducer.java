/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ToTopicProducer {
  private static final Logger log = LoggerFactory.getLogger(ToTopicProducer.class);

  public boolean sendTopic(String data) {
    log.debug("sending data to kafka ... ");
    return true;
  }
}
