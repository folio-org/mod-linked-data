package org.folio.linked.data.integration.kafka.sender.inventory;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.PredicateDictionary.ADMIN_METADATA;
import static org.folio.ld.dictionary.PropertyDictionary.CONTROL_NUMBER;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.LIGHT_RESOURCE;
import static org.folio.linked.data.domain.dto.InstanceIngressEvent.EventTypeEnum.CREATE_INSTANCE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.ResourceUtils.getPropertyValues;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.InstanceIngressEvent;
import org.folio.linked.data.integration.kafka.sender.CreateMessageSender;
import org.folio.linked.data.mapper.kafka.inventory.InstanceIngressMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class InstanceCreateMessageSender implements CreateMessageSender {

  private final InstanceUpdateMessageSender instanceUpdateMessageSender;
  private final InstanceIngressMessageMapper instanceIngressMessageMapper;
  private final FolioMessageProducer<InstanceIngressEvent> instanceIngressMessageProducer;

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (resource.isOfType(LIGHT_RESOURCE) || !resource.isOfType(INSTANCE)) {
      return emptyList();
    }
    if (instanceExistInInventory(resource)) {
      log.info("Resource id {} already exists in inventory. Sending UPDATE_INSTANCE message", resource.getId());
      instanceUpdateMessageSender.produce(resource);
      return emptyList();
    }
    return singletonList(resource);
  }

  @Override
  public void accept(Resource resource) {
    log.debug("Publishing CREATE_INSTANCE message to inventory for instance with ID [{}]", resource.getId());
    var message = instanceIngressMessageMapper.toInstanceIngressEvent(resource)
      .eventType(CREATE_INSTANCE);
    instanceIngressMessageProducer.sendMessages(List.of(message));
  }

  private boolean instanceExistInInventory(Resource resource) {
    return resource.getOutgoingEdges().stream()
      .filter(edge -> ADMIN_METADATA.getUri().equals(edge.getPredicate().getUri()))
      .map(ResourceEdge::getTarget)
      .map(r -> getPropertyValues(r, CONTROL_NUMBER))
      .anyMatch(hrids -> !hrids.isEmpty());
  }
}
