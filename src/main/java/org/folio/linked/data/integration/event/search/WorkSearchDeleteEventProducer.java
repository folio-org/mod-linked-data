package org.folio.linked.data.integration.event.search;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.event.DeleteResourceEventProducer;
import org.folio.linked.data.integration.kafka.sender.search.KafkaSearchSender;
import org.folio.linked.data.model.entity.Resource;
import org.folio.marc4ld.util.ResourceKind;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class WorkSearchDeleteEventProducer implements DeleteResourceEventProducer {

  private final KafkaSearchSender kafkaSearchSender;

  @Override
  public boolean test(Resource resource) {
    return ResourceKind.BIBLIOGRAPHIC
      .stream()
      .anyMatch(resource::isOfType);
  }

  @Override
  public void accept(Resource resource) {
    kafkaSearchSender.sendWorkDeleted(resource);
  }
}
