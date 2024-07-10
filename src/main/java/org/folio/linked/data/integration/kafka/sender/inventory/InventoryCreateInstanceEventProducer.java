package org.folio.linked.data.integration.kafka.sender.inventory;

import static java.util.Optional.ofNullable;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.BibframeUtils.extractInstances;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.inventory.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.util.ResourceKind;
import org.folio.search.domain.dto.InstanceIngressEvent;
import org.folio.search.domain.dto.InstanceIngressPayload;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class InventoryCreateInstanceEventProducer implements CreateMessageSender {

  private final KafkaInventoryMessageMapper kafkaInventoryMessageMapper;
  private final FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;


  @Override
  public Collection<Resource> apply(Resource resource) {
    return Stream.of(resource)
      .filter(this::test)
      .toList();
  }

  //TODO refactoring to extract resources
  private boolean test(Resource resource) {
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
      .forEach(this::sendInstanceCreated);
  }

  public boolean isSourcedFromLinkedData(Resource resource) {
    return ofNullable(resource.getInstanceMetadata())
      .map(InstanceMetadata::getSource)
      .map(source -> source == LINKED_DATA)
      .orElse(false);
  }

  private void sendInstanceCreated(Resource resource) {
    kafkaInventoryMessageMapper.toInstanceIngressPayload(resource)
      .map(this::getCreateInstanceEvent)
      .map(List::of)
      .ifPresent(instanceIngressMessageProducer::sendMessages);
  }

  private InstanceIngressEvent getCreateInstanceEvent(InstanceIngressPayload instanceIngressPayload) {
    return new InstanceIngressEvent()
      .id(UUID.randomUUID().toString())
      .eventType(InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE)
      .eventPayload(instanceIngressPayload);
  }
}
