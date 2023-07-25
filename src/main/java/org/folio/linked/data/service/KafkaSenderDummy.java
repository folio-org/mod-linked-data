package org.folio.linked.data.service;

import static org.folio.linked.data.util.Constants.SEARCH_PROFILE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!" + SEARCH_PROFILE)
@RequiredArgsConstructor
public class KafkaSenderDummy implements KafkaSender {

  @Override
  public void sendResourceCreated(String tenant, BibframeRequest request, Long id) {
  }

}
