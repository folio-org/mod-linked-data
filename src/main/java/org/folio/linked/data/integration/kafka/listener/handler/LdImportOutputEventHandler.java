package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
  private final ResourceModelMapper resourceModelMapper;
  private final ResourceGraphService resourceGraphService;
  private final ResourceEventsPublisher resourceEventsPublisher;

  public void handle(ImportOutputEvent event) {
    event.getResources().forEach(resource -> {
      try {
        var entity = resourceModelMapper.toEntity(resource);
        var saveGraphResult = resourceGraphService.saveMergingGraph(entity);
        resourceEventsPublisher.emitEventsForCreateAndUpdate(saveGraphResult, null);
      } catch (Exception e) {
        log.error("Exception during LD Import output resource with id = {} saving", resource.getId(), e);
      }
    });
  }
}
