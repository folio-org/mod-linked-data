package org.folio.linked.data.integration.kafka.sender.search.authority;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.integration.kafka.sender.ReplaceMessageSender;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class AuthorityReplaceMessageSender implements ReplaceMessageSender {

  private final AuthorityCreateMessageSender authorityCreateMessageSender;
  private final AuthorityDeleteMessageSender authorityDeleteMessageSender;

  @Override
  public Collection<Pair<Resource, Resource>> apply(Resource previous, Resource current) {
    if (current.isNotOfType(HUB) && current.isAuthority()) {
      return singletonList(Pair.of(previous, current));
    }
    return emptyList();
  }

  @Override
  public void accept(Pair<Resource, Resource> pair) {
    log.info("AUTHORITY replace with different Id triggered old AUTHORITY [id {}] index deletion and"
      + " new AUTHORITY [id {}] index creation", pair.getFirst().getId(), pair.getSecond().getId());
    authorityDeleteMessageSender.produce(pair.getFirst());
    authorityCreateMessageSender.produce(pair.getSecond());
  }
}
