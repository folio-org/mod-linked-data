package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.NLM;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationNlm;
import org.folio.linked.data.domain.dto.ClassificationNlmField;
import org.folio.linked.data.domain.dto.Work;
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
  public Work toDto(Resource source, Work destination) {
    var nlm = coreMapper.readResourceDoc(source, ClassificationNlm.class);
    coreMapper.addMappedProperties(source, ASSIGNER_PRED, nlm::addAssignerItem);
    destination.addClassificationItem(new ClassificationNlmField().classificationNlm(nlm));
    return destination;
  }
}
