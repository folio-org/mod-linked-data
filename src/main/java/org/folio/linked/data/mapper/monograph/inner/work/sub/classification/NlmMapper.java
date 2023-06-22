package org.folio.linked.data.mapper.monograph.inner.work.sub.classification;

import static org.folio.linked.data.util.BibframeConstants.ASSIGNER_PRED;
import static org.folio.linked.data.util.BibframeConstants.NLM;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.ClassificationNlm;
import org.folio.linked.data.domain.dto.ClassificationNlmField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(NLM)
public class NlmMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var nlm = readResourceDoc(objectMapper, source, ClassificationNlm.class);
    addMappedProperties(objectMapper, source, ASSIGNER_PRED, nlm::addAssignerItem);
    destination.addClassificationItem(new ClassificationNlmField().classificationNlm(nlm));
    return destination;
  }
}
