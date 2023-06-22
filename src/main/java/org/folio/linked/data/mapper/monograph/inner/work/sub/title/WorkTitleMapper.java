package org.folio.linked.data.mapper.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.BibframeConstants.WORK_TITLE;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.domain.dto.WorkTitle;
import org.folio.linked.data.domain.dto.WorkTitleField;
import org.folio.linked.data.mapper.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(WORK_TITLE)
public class WorkTitleMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var workTitle = readResourceDoc(objectMapper, source, WorkTitle.class);
    destination.addTitleItem(new WorkTitleField().workTitle(workTitle));
    return destination;
  }

}
