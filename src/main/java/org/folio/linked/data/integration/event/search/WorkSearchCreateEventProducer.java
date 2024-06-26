package org.folio.linked.data.integration.event.search;

import static java.lang.String.format;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;
import static org.folio.linked.data.util.Constants.NOT_INDEXED;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.event.CreateResourceEventProducer;
import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.folio.marc4ld.util.ResourceKind;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class WorkSearchCreateEventProducer implements CreateResourceEventProducer {

  private final ResourceRepository resourceRepository;
  private final KafkaSearchSender kafkaSearchSender;

  @Override
  public boolean test(Resource resource) {
    return ResourceKind.BIBLIOGRAPHIC
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  public void accept(Resource resource) {
    if (resource.isOfType(WORK)) {
      kafkaSearchSender.sendWorkCreated(resource);
    } else {
      extractWork(resource)
        .map(Resource::getId)
        .map(resourceRepository::getReferenceById)
        .ifPresentOrElse(kafkaSearchSender::sendWorkCreated,
          () -> log.warn(format(NOT_INDEXED, resource.getId(), "created")));
    }
  }
}
