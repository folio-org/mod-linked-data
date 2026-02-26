package org.folio.linked.data.configuration.batch;

import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.ld.dictionary.ResourceTypeDictionary.WORK;
import static org.folio.linked.data.domain.dto.ResourceIndexEventType.CREATE;
import static org.folio.linked.data.util.Constants.STANDALONE_PROFILE;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceIndexEvent;
import org.folio.linked.data.mapper.kafka.search.HubSearchMessageMapper;
import org.folio.linked.data.mapper.kafka.search.WorkSearchMessageMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
@RequiredArgsConstructor
@Profile("!" + STANDALONE_PROFILE)
public class ReindexProcessor implements ItemProcessor<Resource, ResourceIndexEvent> {

  private final HubSearchMessageMapper hubSearchMessageMapper;
  private final WorkSearchMessageMapper workSearchMessageMapper;

  @Override
  public ResourceIndexEvent process(Resource r) {
    if (r.isOfType(HUB)) {
      return hubSearchMessageMapper.toIndex(r, CREATE);
    }
    if (r.isOfType(WORK)) {
      return workSearchMessageMapper.toIndex(r, CREATE);
    }
    log.warn("Skipping resource with ID [{}] because of unexpected type [{}]", r.getId(), r.getTypes());
    return null;
  }

}
