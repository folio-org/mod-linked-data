package org.folio.linked.data.mapper.monograph.inner.work.sub.content;

import static org.folio.linked.data.util.BibframeConstants.SUPPLEMENTARY_CONTENT_PRED;
import static org.folio.linked.data.util.MappingUtil.toProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(SUPPLEMENTARY_CONTENT_PRED)
public class WorkSupplementaryContentMapper implements WorkSubResourceMapper {

  private final ObjectMapper mapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var property = toProperty(mapper, source);
    destination.addSupplementaryContentItem(property);
    return destination;
  }
}
