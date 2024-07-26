package org.folio.linked.data.integration.kafka.consumer;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.service.ResourceService;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.folio.search.domain.dto.DataImportEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class DataImportEventHandler {

  private final MarcBib2ldMapper marcBib2ldMapper;
  private final MarcAuthority2ldMapper marcAuthority2ldMapper;
  private final ResourceService resourceService;

  public void handle(DataImportEvent event) {
    if (isNotEmpty(event.getMarcBib())) {
      createBibResource(event);
    } else if (isNotEmpty(event.getMarcAuthority())) {
      createAuthorities(event);
    } else {
      log.error("DataImportEvent with id [{}], tenant [{}], eventType [{}] has no Marc record inside",
        event.getId(), event.getTenant(), event.getEventType());
    }
  }

  private void createBibResource(DataImportEvent event) {
    marcBib2ldMapper.fromMarcJson(event.getMarcBib())
      .ifPresentOrElse(resource -> {
          var id = resourceService.createResource(resource);
          log.info("DataImportEvent with id [{}] was saved as LD resource with id [{}]", event.getId(), id);
        },
        () -> log.warn("Empty resource from MARC for event ID [{}]", event.getId())
      );
  }

  private void createAuthorities(DataImportEvent event) {
    var authorityResources = marcAuthority2ldMapper.fromMarcJson(event.getMarcAuthority());
    var ids = authorityResources.stream()
      .map(resourceService::createResource)
      .toList();
    log.info("Processing MARC Authority record with event ID [{}], saved records: {}",
      event.getId(), ids.size());
  }
}
