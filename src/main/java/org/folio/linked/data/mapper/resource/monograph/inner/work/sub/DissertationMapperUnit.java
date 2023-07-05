package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.DISSERTATION_PRED;
import static org.folio.linked.data.util.BibframeConstants.GRANTING_INSTITUTION_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Dissertation;
import org.folio.linked.data.domain.dto.DissertationField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = DISSERTATION_PRED)
public class DissertationMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var dissertation = coreMapper.readResourceDoc(source, Dissertation.class);
    coreMapper.addMappedProperties(source, NOTE_PRED, dissertation::addNoteItem);
    coreMapper.addMappedProperties(source, DATE_PRED, dissertation::addDateItem);
    coreMapper.addMappedProperties(source, GRANTING_INSTITUTION_PRED, dissertation::addGrantingInstitutionItem);
    destination.addDissertationItem(new DissertationField().dissertation(dissertation));
    return destination;
  }
}
