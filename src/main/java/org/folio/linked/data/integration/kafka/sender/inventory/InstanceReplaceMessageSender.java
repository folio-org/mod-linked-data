package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.integration.kafka.sender.ReplaceMessageSender;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class InstanceReplaceMessageSender implements ReplaceMessageSender {

  private final InstanceUpdateMessageSender updateInstanceEventProducer;

  @Override
  public Collection<Pair<Resource, Resource>> apply(Resource previous, Resource current) {
    return updateInstanceEventProducer.apply(current)
      .stream()
      .map(applied -> Pair.of(previous, applied))
      .toList();
  }

  @Override
  public void accept(Pair<Resource, Resource> pair) {
    updateInstanceEventProducer.accept(pair.getSecond());
  }

}
