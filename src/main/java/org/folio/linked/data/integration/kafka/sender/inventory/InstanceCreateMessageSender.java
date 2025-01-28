package org.folio.linked.data.integration.kafka.sender.inventory;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.linked.data.domain.dto.InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE;
import static org.folio.linked.data.model.entity.ResourceSource.LINKED_DATA;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.FolioMetadata;
import org.folio.linked.data.model.entity.Resource;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class InstanceCreateMessageSender implements CreateMessageSender {

  private final InstanceIngressMessageMapper instanceIngressMessageMapper;
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
    log.info("Publishing CREATE_INSTANCE message to inventory for instance with ID [{}]", resource.getId());
    var message = instanceIngressMessageMapper.toInstanceIngressEvent(resource)
      .eventType(CREATE_INSTANCE);
    instanceIngressMessageProducer.sendMessages(List.of(message));
  }

  private boolean isSourcedFromLinkedData(Resource resource) {
    return ofNullable(resource.getFolioMetadata())
      .map(FolioMetadata::getSource)
      .map(source -> source == LINKED_DATA)
      .orElse(false);
  }

}
