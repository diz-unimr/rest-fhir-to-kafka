/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka.input;

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
    log.debug("received data: {%s}".formatted(data));

    if (!manager.transformAndProduce(data)) {
      throw new RuntimeException("data could not be proccessed");
    }

    return ResponseEntity.accepted().build();
  }
}
