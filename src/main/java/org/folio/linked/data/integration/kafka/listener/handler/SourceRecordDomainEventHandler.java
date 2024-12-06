package org.folio.linked.data.integration.kafka.listener.handler;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.CREATED;
import static org.folio.linked.data.domain.dto.SourceRecordDomainEvent.EventTypeEnum.UPDATED;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_AUTHORITY;
import static org.folio.linked.data.domain.dto.SourceRecordType.MARC_BIB;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ld.dictionary.model.Resource;
import org.folio.linked.data.domain.dto.SourceRecordDomainEvent;
import org.folio.linked.data.domain.dto.SourceRecordType;
import org.folio.linked.data.service.resource.ResourceMarcAuthorityService;
import org.folio.linked.data.service.resource.ResourceMarcBibService;
import org.folio.marc4ld.service.marc2ld.authority.MarcAuthority2ldMapper;
import org.folio.marc4ld.service.marc2ld.bib.MarcBib2ldMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class SourceRecordDomainEventHandler {

  private static final String EVENT_SAVED = "SourceRecordDomainEvent [id {}] was saved as {} resource [id {}]";
  private static final String EMPTY_RESOURCE_MAPPED = "Empty resource(s) mapped from SourceRecordDomainEvent [id {}]";
  private static final String NO_MARC_EVENT = "SourceRecordDomainEvent [id {}] has no Marc record inside";
  private static final String UNSUPPORTED_TYPE = "Ignoring unsupported {} type [{}] in SourceRecordDomainEvent [id {}]";
  private static final Set<SourceRecordType> SUPPORTED_RECORD_TYPES = Set.of(MARC_BIB, MARC_AUTHORITY);
  private static final Set<SourceRecordDomainEvent.EventTypeEnum> SUPPORTED_EVENT_TYPES = Set.of(CREATED, UPDATED);
  private final ResourceMarcAuthorityService resourceMarcAuthorityService;
  private final ResourceMarcBibService resourceMarcBibService;
  private final MarcAuthority2ldMapper marcAuthority2ldMapper;
  private final MarcBib2ldMapper marcBib2ldMapper;

  @SuppressWarnings("java:S125")
  public void handle(SourceRecordDomainEvent event, SourceRecordType recordType) {
    if (notProcessableEvent(event, recordType)) {
      return;
    }
    if (recordType == MARC_AUTHORITY) {
      saveAuthorities(event);
    } else if (recordType == MARC_BIB) {
      saveAdminMetadata(event);
    }
  }

  private boolean notProcessableEvent(SourceRecordDomainEvent event, SourceRecordType recordType) {
    if (isEmpty(event.getEventPayload())
      || isEmpty(event.getEventPayload().getParsedRecord())
      || isEmpty(event.getEventPayload().getParsedRecord().getContent())) {
      log.warn(NO_MARC_EVENT, event.getId());
      return true;
    }
    if (isNull(recordType) || !SUPPORTED_RECORD_TYPES.contains(recordType)) {
      logUnsupportedType(event, "record", recordType);
      return true;
    }
    if (isNull(event.getEventType()) || !SUPPORTED_EVENT_TYPES.contains(event.getEventType())) {
      logUnsupportedType(event, "event", event.getEventType());
      return true;
    }
    return false;
  }

  private void saveAuthorities(SourceRecordDomainEvent event) {
    var mapped = marcAuthority2ldMapper.fromMarcJson(event.getEventPayload().getParsedRecord().getContent());
    if (mapped.isEmpty()) {
      logEmptyResource(event.getId());
    } else {
      mapped.forEach(resource -> saveAuthority(resource, event));
    }
  }

  private void saveAuthority(Resource resource, SourceRecordDomainEvent event) {
    if (CREATED == event.getEventType() || UPDATED == event.getEventType()) {
      var id = resourceMarcAuthorityService.saveMarcResource(resource);
      log.info(EVENT_SAVED, event.getId(), MARC_AUTHORITY, id);
    }
  }

  private void saveAdminMetadata(SourceRecordDomainEvent event) {
    marcBib2ldMapper.fromMarcJson(event.getEventPayload().getParsedRecord().getContent())
      .ifPresentOrElse(mapped -> {
          if (resourceMarcBibService.saveAdminMetadata(mapped)) {
            log.info("AdminMetadata has been added to resource with id [{}], SourceRecordDomainEvent id [{}]",
              mapped.getId(), event.getId());
          }
        },
        () -> log.info("SourceRecordDomainEvent with id [{}] doesn't contain AdminMetadata fields, skipping",
          event.getId())
      );
  }

  private void logUnsupportedType(SourceRecordDomainEvent event, String typeKind, Enum<?> type) {
    log.info(UNSUPPORTED_TYPE, typeKind, isNull(type) ? "null" : type.name(), event.getId());
  }

  private void logEmptyResource(String eventId) {
    log.info(EMPTY_RESOURCE_MAPPED, eventId);
  }

}
