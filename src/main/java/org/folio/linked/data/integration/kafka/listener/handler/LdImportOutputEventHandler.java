package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    log.debug("Handling LD Import output event with id {} for tenant {}", event.getTs(), event.getTenant());
    var counter = new AtomicInteger();
    event.getResources().forEach(resource -> {
      try {
        resourceService.saveResource(resource);
        counter.getAndIncrement();
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception [{}] during saving LDImport resource: {}", e.getMessage(), resource);
        } else {
          log.error("Exception [{}] during saving LDImport resource with id [{}]", e.getMessage(), resource.getId());
        }
      }
    });
    log.info("{} of {} resource(s) saved out of LDImportOutput event with ID {} for tenant {}", counter.get(),
      event.getResources().size(), event.getTs(), event.getTenant());
  }

}
