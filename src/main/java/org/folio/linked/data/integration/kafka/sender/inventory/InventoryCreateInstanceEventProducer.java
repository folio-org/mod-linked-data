package org.folio.linked.data.integration.kafka.sender.inventory;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.search.domain.dto.InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.inventory.KafkaInventoryMessageMapper;
import org.folio.linked.data.model.entity.InstanceMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.search.domain.dto.InstanceIngressEvent;
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
    if (resource.isOfType(INSTANCE) && isSourcedFromLinkedData(resource)) {
      return singletonList(resource);
    }
    return emptyList();
  }

  @Override
  @SneakyThrows
  public void accept(Resource resource) {
    kafkaInventoryMessageMapper.toInstanceIngressEvent(resource)
      .map(e -> e.eventType(CREATE_INSTANCE))
      .map(List::of)
      .ifPresent(instanceIngressMessageProducer::sendMessages);
  }

  private boolean isSourcedFromLinkedData(Resource resource) {
    return ofNullable(resource.getInstanceMetadata())
      .map(InstanceMetadata::getSource)
      .map(source -> source == LINKED_DATA)
      .orElse(false);
  }

}
