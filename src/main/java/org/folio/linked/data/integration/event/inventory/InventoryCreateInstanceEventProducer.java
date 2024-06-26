package org.folio.linked.data.integration.event.inventory;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.BibframeUtils.extractInstances;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.integration.event.CreateResourceEventProducer;
import org.folio.linked.data.integration.kafka.sender.inventory.KafkaInventorySender;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.util.ResourceKind;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class InventoryCreateInstanceEventProducer implements CreateResourceEventProducer {

  private final KafkaInventorySender kafkaInventorySender;

  @Override
  public boolean test(Resource resource) {
    return ResourceKind.BIBLIOGRAPHIC
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  @SneakyThrows
  public void accept(Resource resource) {
    extractInstances(resource)
      .stream()
      .filter(this::isSourcedFromLinkedData)
      .forEach(kafkaInventorySender::sendInstanceCreated);
  }

  public boolean isSourcedFromLinkedData(Resource resource) {
    return ofNullable(resource.getInstanceMetadata())
      .map(InstanceMetadata::getSource)
      .map(source -> source == LINKED_DATA)
      .orElse(false);
  }
}
