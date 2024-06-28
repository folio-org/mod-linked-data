package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile("!" + FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaSearchSenderDummy implements KafkaSearchSender {

  @Override
  public void sendWorkCreated(Resource resource) {
    log.debug("sendSingleResourceCreated is ignored by dummy kafka sender, because search feature is disabled");
  }

  @Override
  public boolean sendMultipleWorksCreated(Resource resource) {
    log.debug("sendMultipleResourceCreated is ignored by dummy kafka sender, because search feature is disabled");
    return false;
  }

  @Override
  public void sendWorkUpdated(Resource newResource, Resource oldResource) {
    log.debug("sendResourceUpdated is ignored by dummy kafka sender, because search feature is disabled");
  }

  @Override
  public void sendWorkDeleted(Resource resource) {
    log.debug("sendResourceDeleted is ignored by dummy kafka sender, because search feature is disabled");
  }

  @Override
  public void sendAuthorityCreated(Resource resource) {
    log.debug("sendAuthorityCreated is ignored by dummy kafka sender, because search feature is disabled");
  }
}
