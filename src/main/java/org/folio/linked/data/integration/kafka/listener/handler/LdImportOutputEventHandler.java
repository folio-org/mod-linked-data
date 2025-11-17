package org.folio.linked.data.integration.kafka.listener.handler;

import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ImportOutputEvent;
import org.folio.linked.data.service.rdf.RdfImportService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class LdImportOutputEventHandler implements ExternalEventHandler<ImportOutputEvent> {
  private final RdfImportService rdfImportService;

  public void handle(ImportOutputEvent event, LocalDateTime startTime) {
    log.info("Handling LD Import output event with Job ID {} and ts {}", event.getJobInstanceId(), event.getTs());
    rdfImportService.importOutputEvent(event, startTime);
  }

}
