package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.Bibframe2Constants.DATE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.LANGUAGE_PRED;
import static org.folio.linked.data.util.Bibframe2Constants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Language2;
import org.folio.linked.data.domain.dto.LanguageField2;
import org.folio.linked.data.domain.dto.Work2;
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
  public Work2 toDto(Resource source, Work2 destination) {
    var language = coreMapper.readResourceDoc(source, Language2.class);
    coreMapper.addMappedProperties(source, NOTE_PRED, language::addPartItem);
    coreMapper.addMappedProperties(source, DATE_PRED, language::addSameAsItem);
    destination.addLanguageItem(new LanguageField2().language(language));
    return destination;
  }
}
