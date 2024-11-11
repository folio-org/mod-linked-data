package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static org.folio.ld.dictionary.PredicateDictionary.INSTANTIATES;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.DELETE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;
import static org.folio.linked.data.util.ResourceUtils.extractWorkFromInstance;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.integration.kafka.sender.DeleteMessageSender;
import org.folio.linked.data.mapper.kafka.search.WorkSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceEdge;
import org.folio.spring.tools.kafka.FolioMessageProducer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class WorkDeleteMessageSender implements DeleteMessageSender {

  @Qualifier("bibliographicMessageProducer")
  private final FolioMessageProducer<ResourceIndexEvent> bibliographicMessageProducer;
  private final WorkSearchMessageMapper workSearchMessageMapper;
  private final WorkUpdateMessageSender workUpdateMessageSender;

  @Override
  public Collection<Resource> apply(Resource resource) {
    if (resource.isOfType(WORK)) {
      return List.of(resource);
    }
    if (resource.isOfType(INSTANCE)) {
      triggerParentWorkUpdate(resource);
    }
    return emptyList();
  }

  @Override
  public void accept(Resource resource) {
    var onlyIdResource = new Resource().setId(resource.getId());
    var message = workSearchMessageMapper.toIndex(onlyIdResource)
      .type(DELETE);
    bibliographicMessageProducer.sendMessages(List.of(message));
  }

  private void triggerParentWorkUpdate(Resource instance) {
    extractWorkFromInstance(instance)
      .ifPresentOrElse(work -> {
        log.info("Instance [id {}] deletion triggered parent Work [id {}] index update", instance.getId(),
          work.getId());
        var newWork = new Resource(work);
        newWork.getIncomingEdges().remove(new ResourceEdge(instance, work, INSTANTIATES));
        workUpdateMessageSender.produce(newWork);
      }, () -> log.error("Instance [id {}] deleted, but parent work wasn't found!", instance.getId()));
  }

}
