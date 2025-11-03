package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.service.resource.events.ResourceEventsPublisher;
import org.folio.linked.data.service.resource.graph.ResourceGraphService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class LdImportOutputEventHandler implements ExternalEventHandler<ImportOutputEvent> {
  private final ObjectMapper objectMapper;
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final ResourceEventsPublisher resourceEventsPublisher;

  public void handle(ImportOutputEvent event) {
    log.info("Handling LD Import output event with id {} for tenant {}", event.getTs(), event.getTenant());
    event.getResources().forEach(rawResource -> {
      try {
        var resource = objectMapper.readValue(rawResource, Resource.class);
        log.debug("Saving LD Import output resource with id = {}", resource.getId());
        var entity = resourceModelMapper.toEntity(resource);
        var saveGraphResult = resourceGraphService.saveMergingGraph(entity);
        log.debug("Sending create/update events for LD Import output resource with id = {}", resource.getId());
        resourceEventsPublisher.emitEventsForCreateAndUpdate(saveGraphResult, null);
      } catch (Exception e) {
        var id = readId(rawResource);
        log.error("Exception during LD Import output resource with id = {} saving", id, e);
      }
    });
  }

  private String readId(String rawResource) {
    try {
      return objectMapper.readTree(rawResource).get("id").asText();
    } catch (JsonProcessingException e) {
      return "unknown-id";
    }
  }
}
