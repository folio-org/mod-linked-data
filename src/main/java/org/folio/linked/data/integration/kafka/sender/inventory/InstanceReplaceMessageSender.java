package org.folio.linked.data.integration.kafka.sender.inventory;

import static org.folio.linked.data.util.Constants.FOLIO_PROFILE;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.folio.linked.data.integration.kafka.sender.ReplaceMessageSender;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Profile(FOLIO_PROFILE)
@RequiredArgsConstructor
public class InstanceReplaceMessageSender implements ReplaceMessageSender {

  private final InstanceUpdateMessageSender instanceUpdateMessageSender;

  @Override
  public void produce(Resource previous, Resource current) {
    instanceUpdateMessageSender.produce(current);
  }

  @Override
  public Collection<Pair<Resource, Resource>> apply(Resource resource, Resource resource2) {
    throw new NotImplementedException();
  }

  @Override
  public void accept(Pair<Resource, Resource> resourceResourcePair) {
    throw new NotImplementedException();
  }
}
