/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka.input;

import ca.uhn.fhir.parser.DataFormatException;
import de.unimarburg.diz.rest_fhir_to_kafka.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestFhirController {

  private static final Logger log = LoggerFactory.getLogger(RestFhirController.class);

  private final ProcessManager manager;

  public RestFhirController(ProcessManager manager) {
    this.manager = manager;
  }

  @PostMapping("/fhirIn")
  public ResponseEntity<String> receiveFhirData(@RequestBody String data) {
    log.debug("receiving data starting with: %s...".formatted(data.substring(0, 20)));
    boolean wasSuccessful;
    try {
      wasSuccessful = !manager.transformAndProduce(data);
    } catch (DataFormatException dataFormatException) {
      log.error("processing data failed");
      return ResponseEntity.badRequest().body(dataFormatException.getLocalizedMessage());
    }
    if (wasSuccessful) {
      throw new RuntimeException("data could not be processed");
    }

    return ResponseEntity.accepted().build();
  }
}
