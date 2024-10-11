package org.folio.linked.data.integration.kafka.listener.handler;

import static org.apache.commons.lang3.ObjectUtils.anyNull;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.UPDATE;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

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
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class InventoryInstanceEventHandler {

  public static final String INSTANCE_REINDEX_NOT_REQUIRED = "Ignoring InventoryInstanceEvent '{}',"
    + " reindexing not required.";
  public static final String SENT_FOR_REINDEXING = "Instance '{}' has some fields updated, sent for reindexing.";
  public static final String SUPPRESS_FLAGS_CHANGED = "InventoryInstanceEvent:{} - changes in suppress flags: {}.";

  private final FolioMetadataRepository folioMetadataRepository;
  private final WorkUpdateMessageSender workUpdateMessageSender;

  @Transactional
  public void handle(InventoryInstanceEvent event) {
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
    folioMetadata.setStaffSuppress(event.getNew().getStaffSuppress());
    folioMetadata.setSuppressFromDiscovery(event.getNew().getDiscoverySuppress());
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
