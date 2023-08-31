package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.Bibframe2Constants.ASSIGNER_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NLM;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationNlm2;
import org.folio.linked.data.domain.dto.ClassificationNlmField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = NLM)
public class NlmMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var nlm = coreMapper.readResourceDoc(source, ClassificationNlm2.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, nlm::addAssignerItem);
    destination.addClassificationItem(new ClassificationNlmField2().classificationNlm(nlm));
    return destination;
  }
}
