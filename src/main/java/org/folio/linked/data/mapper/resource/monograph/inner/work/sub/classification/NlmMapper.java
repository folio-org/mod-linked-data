package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.NLM;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationNlm;
import org.folio.linked.data.domain.dto.ClassificationNlmField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = NLM)
public class NlmMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var nlm = commonMapper.readResourceDoc(source, ClassificationNlm.class);
    commonMapper.addMappedProperties(source, ASSIGNER_PRED, nlm::addAssignerItem);
    destination.addClassificationItem(new ClassificationNlmField().classificationNlm(nlm));
    return destination;
  }
}
