package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.LANGUAGE_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.LanguageField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = LANGUAGE_PRED)
public class LanguageMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var language = coreMapper.readResourceDoc(source, Language.class);
    coreMapper.addMappedProperties(source, NOTE_PRED, language::addPartItem);
    coreMapper.addMappedProperties(source, DATE_PRED, language::addSameAsItem);
    destination.addLanguageItem(new LanguageField().language(language));
    return destination;
  }
}
