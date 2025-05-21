/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka;

import ca.uhn.fhir.context.FhirContext;
import de.unimarburg.diz.rest_fhir_to_kafka.output.ToTopicProducer;
import org.hl7.fhir.instance.model.api.IBaseResource;
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

    // should be xml
    final var transformedResource = dataToResource(data);
    final var resourceAsJson = transformToJson(transformedResource);

    if (producer.sendTopic(resourceAsJson)) {
      return true;
    }

    return false;
  }

  private String transformToJson(IBaseResource resource) {

    var jsonParser = fhirContext.newJsonParser();
    var asJson = jsonParser.encodeResourceToString(resource);

    log.debug("resource encoded to json");
    return asJson;
  }

  private IBaseResource dataToResource(String data) {
    var newXmlParser = fhirContext.newXmlParser();

    var transformedResource = newXmlParser.parseResource(data);
    log.debug("data transformed to resource");
    return transformedResource;
  }
}
