package org.folio.linked.data.integration.kafka.sender.search;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.folio.ld.dictionary.ResourceTypeDictionary.INSTANCE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.util.BibframeUtils.extractWork;
import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

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
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class WorkReplaceMessageSender implements ReplaceMessageSender {

  private final WorkDeleteMessageSender workDeleteMessageSender;
  private final WorkCreateMessageSender workCreateMessageSender;
  private final WorkUpdateMessageSender workUpdateMessageSender;

  @Override
  public Collection<Pair<Resource, Resource>> apply(Resource previous, Resource current) {
    if (current.isOfType(WORK)) {
      return singletonList(Pair.of(previous, current));
    }
    if (current.isOfType(INSTANCE)) {
      triggerParentWorkUpdate(current);
    }
    return emptyList();
  }

  @Override
  public void accept(Pair<Resource, Resource> pair) {
    log.info("Work replace with different Id triggered old Work [id {}] index deletion and new Work [id [{}]] "
      + "index creation", pair.getFirst().getId(), pair.getSecond().getId());
    workDeleteMessageSender.produce(pair.getFirst());
    workCreateMessageSender.produce(pair.getSecond());
  }

  private void triggerParentWorkUpdate(Resource instance) {
    extractWork(instance)
      .ifPresentOrElse(work -> {
          log.info("Instance [id {}] replace triggered parent Work [{}] index update", instance.getId(), work.getId());
          workUpdateMessageSender.produce(work);
        },
        () -> log.error("Instance [id {}] replaced, but parent work wasn't found!", instance.getId())
      );
  }

}
