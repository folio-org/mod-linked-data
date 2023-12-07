package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.search.domain.dto.BibframeIndex;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@Profile("!" + SEARCH_PROFILE)
@RequiredArgsConstructor
public class KafkaSenderDummy implements KafkaSender {

  @Override
  public void sendResourceCreated(BibframeIndex bibframeIndex, boolean isSingle) {
    log.debug("sendResourceCreated is ignored by dummy kafka sender, because search feature is disabled");
  }

  @Override
  public void sendResourceDeleted(Long id) {
    log.debug("sendResourceDeleted is ignored by dummy kafka sender, because search feature is disabled");
  }
}
