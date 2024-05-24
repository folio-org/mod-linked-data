package org.folio.linked.data.integration.kafka.sender.inventory;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

@Log4j2
@Service
@Profile("!" + FOLIO_PROFILE)
@RequiredArgsConstructor
public class KafkaInventorySenderDummy implements KafkaInventorySender {

  @Override
  public void sendInstanceCreated(Resource resource) {
    log.debug("sendInstanceCreated is ignored by dummy kafka sender, because Folio profile is disabled");
  }

}
