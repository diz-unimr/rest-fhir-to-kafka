/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.rest_fhir_to_kafka.input;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.uhn.fhir.context.FhirContext;
import de.unimarburg.diz.rest_fhir_to_kafka.output.ToTopicProducer;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RestFhirControllerTest {

  @Autowired MockMvc mockMvc;

  @Autowired FhirContext fhirContext;

  @MockitoBean(enforceOverride = true)
  private ToTopicProducer toTopicProducer;

  @Test
  void receiveFhirData() throws Exception {

    var xmlParser = fhirContext.newXmlParser();

    var xmlResource =
        xmlParser.encodeResourceToString(
            new Patient()
                .setActive(true)
                .addName(new HumanName().addGiven("Lana").setFamily("Musterfrau")));

    when(toTopicProducer.sendTopic(any())).thenReturn(true);

    mockMvc
        .perform(post("/fhir").contentType(APPLICATION_XML_VALUE).content(xmlResource))
        .andExpect(status().isAccepted());
  }

  @Test
  void receiveFhirErrorData() throws Exception {

    var xmlResource = "<<<>>";

    mockMvc
        .perform(post("/fhir").contentType(APPLICATION_XML_VALUE).content(xmlResource))
        .andExpect(status().isBadRequest());
  }
}
