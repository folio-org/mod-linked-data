package org.folio.linked.data.integration.kafka.sender.search;

import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import java.util.Collection;
import java.util.List;
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
public class HubReplaceMessageSender implements ReplaceMessageSender {

  private final HubCreateMessageSender hubCreateMessageSender;
  private final HubDeleteMessageSender hubDeleteMessageSender;

  @Override
  public Collection<Pair<Resource, Resource>> apply(Resource previous, Resource current) {
    if (current.isOfType(HUB) && current.getTypes().size() == 1) {
      return List.of(Pair.of(previous, current));
    }
    return List.of();
  }

  @Override
  public void accept(Pair<Resource, Resource> pair) {
    log.info("HUB replace with different Id triggered old HUB [id {}] index deletion and new HUB [id {}] "
      + "index creation", pair.getFirst().getId(), pair.getSecond().getId());
    hubDeleteMessageSender.produce(pair.getFirst());
    hubCreateMessageSender.produce(pair.getSecond());
  }
}
