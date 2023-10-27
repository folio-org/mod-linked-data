package org.folio.linked.data.integration.consumer;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.service.ResourceService;
import org.folio.marc2ld.mapper.Marc2BibframeMapper;
import org.folio.search.domain.dto.DataImportEvent;
import org.folio.spring.FolioExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class DataImportEventHandler {

  private final Marc2BibframeMapper marc2BibframeMapper;
  private final ResourceService resourceService;
  @Autowired
  private FolioExecutionContext folioExecutionContext;

  public void handle(DataImportEvent event) {
    if (isNotEmpty(event.getMarc())) {
      var marc2ldResource = marc2BibframeMapper.map(event.getMarc());
      Long id = resourceService.createResource(marc2ldResource);
      log.info("DataImportEvent with id [{}] was saved as LD resource with id [{}]", event.getId(), id);
    } else {
      log.error("DataImportEvent with id [{}], tenant [{}], eventType [{}] has no Marc record inside",
        event.getId(), event.getTenant(), event.getEventType());
    }
  }
}
