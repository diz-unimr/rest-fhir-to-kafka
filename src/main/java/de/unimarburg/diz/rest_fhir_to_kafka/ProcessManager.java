/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka;

import ca.uhn.fhir.context.FhirContext;
import de.unimarburg.diz.rest_fhir_to_kafka.output.ToTopicProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessManager {

  private static final Logger log = LoggerFactory.getLogger(ProcessManager.class);

  private final ToTopicProducer producer;
  private final FhirContext fhirContext;

  public ProcessManager(ToTopicProducer producer, FhirContext fhirContext) {
    this.producer = producer;
    this.fhirContext = fhirContext;
  }

  public boolean transformAndProduce(String data) {
    log.debug("received data string");
    final var asJson = transformXmlToJson(data);
    log.debug("data transformed to json");
    if (producer.sendTopic(asJson)) {
      return true;
    }

    return false;
  }

  private String transformXmlToJson(String data) {
    var newXmlParser = fhirContext.newXmlParser();
    var jsonParser = fhirContext.newJsonParser();

    var transformedResource = newXmlParser.parseResource(data);

    var asJson = jsonParser.encodeResourceToString(transformedResource);
    return asJson;
  }
}
