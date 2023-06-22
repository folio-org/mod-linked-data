package org.folio.linked.data.mapper.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.DISSERTATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.GRANTING_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.DissertationField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(DISSERTATION_PRED)
public class DissertationMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var dissertation = readResourceDoc(objectMapper, source, Dissertation.class);
    addMappedProperties(objectMapper, source, NOTE_PRED, dissertation::addNoteItem);
    addMappedProperties(objectMapper, source, DATE_PRED, dissertation::addDateItem);
    addMappedProperties(objectMapper, source, GRANTING_INSTITUTION_PRED, dissertation::addGrantingInstitutionItem);
    destination.addDissertationItem(new DissertationField().dissertation(dissertation));
    return destination;
  }
}
