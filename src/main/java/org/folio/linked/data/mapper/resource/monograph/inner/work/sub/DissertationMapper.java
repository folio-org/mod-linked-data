package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.DISSERTATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.GRANTING_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.DissertationField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = DISSERTATION_PRED)
public class DissertationMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var dissertation = commonMapper.readResourceDoc(source, Dissertation.class);
    commonMapper.addMappedProperties(source, NOTE_PRED, dissertation::addNoteItem);
    commonMapper.addMappedProperties(source, DATE_PRED, dissertation::addDateItem);
    commonMapper.addMappedProperties(source, GRANTING_INSTITUTION_PRED, dissertation::addGrantingInstitutionItem);
    destination.addDissertationItem(new DissertationField().dissertation(dissertation));
    return destination;
  }
}
