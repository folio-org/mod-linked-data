package org.folio.linked.data.integration.kafka.listener.handler;

import static org.apache.commons.lang3.ObjectUtils.anyNull;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.mapper.kafka.inventory.InventoryInstanceMapper;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class InventoryInstanceEventHandler {

  public static final String INSTANCE_REINDEX_NOT_REQUIRED = "Ignoring InventoryInstanceEvent '{}',"
    + " reindexing not required.";
  public static final String SENT_FOR_REINDEXING = "Instance '{}' has some fields updated, sent for reindexing.";
  public static final String SUPPRESS_FLAGS_CHANGED = "InventoryInstanceEvent:{} - changes in suppress flags: {}.";

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final ApplicationEventPublisher eventPublisher;
  private final InventoryInstanceMapper inventoryInstanceMapper;
  private final FolioMetadataRepository folioMetadataRepository;

  public void handle(InventoryInstanceEvent event) {
    if (reindexNotRequired(event)) {
      log.debug(INSTANCE_REINDEX_NOT_REQUIRED, event.getId());
      return;
    }
    sendReindexMessage(event);
    publishReindexEvent(event);
    log.info(SENT_FOR_REINDEXING, event.getNew().getId());
  }

  private void sendReindexMessage(InventoryInstanceEvent event) {
    var message = inventoryInstanceMapper.toReindexEvent(event);
    bibliographicMessageProducer.sendMessages(List.of(message));
  }

  private void publishReindexEvent(InventoryInstanceEvent event) {
    eventPublisher.publishEvent(new ResourceIndexEvent().id(event.getNew().getId()));
  }

  private boolean reindexNotRequired(InventoryInstanceEvent event) {
    if (notEqual(event.getType(), UPDATE) || anyNull(event.getOld(), event.getNew())) {
      return true;
    }
    var suppressFlagsChanged = suppressFlagsChanged(event);
    if (!suppressFlagsChanged) {
      return true;
    }
    return !folioMetadataRepository.existsByInventoryId(event.getNew().getId());
  }

  private boolean suppressFlagsChanged(InventoryInstanceEvent event) {
    var newObj = event.getNew();
    var oldObj = event.getOld();
    var result = notEqual(newObj.getStaffSuppress(), oldObj.getStaffSuppress())
      || notEqual(newObj.getDiscoverySuppress(), oldObj.getDiscoverySuppress());
    log.debug(SUPPRESS_FLAGS_CHANGED, event.getId(), result);
    return result;
  }

  private boolean notEqual(Object first, Object second) {
    return !Objects.equals(first, second);
  }
}
