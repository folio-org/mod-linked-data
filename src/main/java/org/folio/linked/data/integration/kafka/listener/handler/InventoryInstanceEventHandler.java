package org.folio.linked.data.integration.kafka.listener.handler;

import static org.apache.commons.lang3.ObjectUtils.anyNull;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InventoryInstanceEvent;
import org.folio.linked.data.integration.kafka.sender.search.WorkUpdateMessageSender;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.FolioMetadataRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class InventoryInstanceEventHandler implements ExternalEventHandler<InventoryInstanceEvent> {

  private static final String INSTANCE_REINDEX_NOT_REQUIRED = "Ignoring InventoryInstanceEvent '{}',"
    + " reindexing not required.";
  private static final String SENT_FOR_REINDEXING = "Instance '{}' has some fields updated, sent for reindexing.";
  private static final String SUPPRESS_FLAGS_CHANGED = "InventoryInstanceEvent:{} - changes in suppress flags: {}.";
  private static final String UPDATING_SUPPRESS_FLAGS = "Updating suppress flags for instance {}. Staff: {}, "
    + "discovery: {}";

  private final FolioMetadataRepository folioMetadataRepository;
  private final WorkUpdateMessageSender workUpdateMessageSender;

  @Transactional
  public void handle(InventoryInstanceEvent event, LocalDateTime startTime) {
    getOptionalReindexResource(event)
      .ifPresentOrElse(this::reindexResource,
        () -> log.debug(INSTANCE_REINDEX_NOT_REQUIRED, event.getId()));
  }

  private Optional<Resource> getOptionalReindexResource(InventoryInstanceEvent event) {
    if (reindexNotRequired(event)) {
      return Optional.empty();
    }
    return folioMetadataRepository.findByInventoryId(event.getNew().getId())
      .map(metadata -> updateMetadataAndGetResource(metadata, event));
  }

  private void reindexResource(Resource resource) {
    workUpdateMessageSender.produce(resource);
    log.info(SENT_FOR_REINDEXING, resource.getId());
  }

  private Resource updateMetadataAndGetResource(FolioMetadata folioMetadata, InventoryInstanceEvent event) {
    var instance = event.getNew();
    log.info(UPDATING_SUPPRESS_FLAGS, instance.getId(), instance.getStaffSuppress(), instance.getDiscoverySuppress());
    folioMetadata.setStaffSuppress(instance.getStaffSuppress());
    folioMetadata.setSuppressFromDiscovery(instance.getDiscoverySuppress());
    return folioMetadataRepository.save(folioMetadata).getResource();
  }

  private boolean reindexNotRequired(InventoryInstanceEvent event) {
    if (notEqual(event.getType(), UPDATE) || anyNull(event.getOld(), event.getNew())) {
      return true;
    }
    return !suppressFlagsChanged(event);
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
