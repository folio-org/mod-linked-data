package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile("!" + SEARCH_PROFILE)
@RequiredArgsConstructor
public class KafkaSearchSenderDummy implements KafkaSearchSender {

  @Override
  public void sendSingleResourceCreated(Resource resource) {
    log.debug("sendSingleResourceCreated is ignored by dummy kafka sender, because search feature is disabled");
  }

  @Override
  public boolean sendMultipleResourceCreated(Resource resource) {
    log.debug("sendMultipleResourceCreated is ignored by dummy kafka sender, because search feature is disabled");
    return false;
  }

  @Override
  public void sendResourceUpdated(Resource newResource, Resource oldResource) {
    log.debug("sendResourceUpdated is ignored by dummy kafka sender, because search feature is disabled");
  }

  @Override
  public void sendResourceDeleted(Resource resource) {
    log.debug("sendResourceDeleted is ignored by dummy kafka sender, because search feature is disabled");
  }
}
