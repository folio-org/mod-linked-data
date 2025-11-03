package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.service.resource.ResourceService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class LdImportOutputEventHandler implements ExternalEventHandler<ImportOutputEvent> {
  private final ResourceService resourceService;

  public void handle(ImportOutputEvent event) {
    log.info("Handling LD Import output event with id {} for tenant {}", event.getTs(), event.getTenant());
    event.getResources().forEach(rawResource -> {
      try {
        var resource = new ObjectMapper().readValue(rawResource, Resource.class);
        resourceService.saveResource(resource);
      } catch (Exception e) {
        var id = readId(rawResource);
        log.error("Exception during LD Import output resource with id = {} saving", id, e);
      }
    });
  }

  private String readId(String rawResource) {
    try {
      return new ObjectMapper().readTree(rawResource).get("id").asText();
    } catch (JsonProcessingException e) {
      return "unknown-id";
    }
  }
}
