/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka;

import ca.uhn.fhir.context.FhirContext;
import de.unimarburg.diz.rest_fhir_to_kafka.output.ToTopicProducer;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.Md5Crypt;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProcessManager {

  private static final Logger log = LoggerFactory.getLogger(ProcessManager.class);

  private final ToTopicProducer producer;
  private final FhirContext fhirContext;
  private final String idSalt;

  public ProcessManager(
      ToTopicProducer producer, FhirContext fhirContext, @Value("${app.id-salt}") String idSalt) {
    this.producer = producer;
    this.fhirContext = fhirContext;
    this.idSalt = idSalt;
  }

  public boolean transformAndProduce(String data) {
    log.debug("received data string");

    // should be xml
    final var transformedResource = dataToResource(data);

    String msgId = extractMessageIdFromBundle(transformedResource);

    final var resourceAsJson = transformToJson(transformedResource);

    if (producer.sendTopic(msgId, resourceAsJson)) {
      return true;
    }

    return false;
  }

  private String extractMessageIdFromBundle(IBaseResource transformedResource) {
    if (transformedResource instanceof Bundle) {
      var joinedIds =
          ((Bundle) transformedResource)
              .getEntry().stream().map(a -> a.getResource().getId()).collect(Collectors.joining());
      Md5Crypt.md5Crypt(joinedIds.getBytes(), idSalt);
    }
    return null;
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
