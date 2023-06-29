package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.DATE_PRED;
import static org.folio.linked.data.util.BibframeConstants.LANGUAGE_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.LanguageField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = LANGUAGE_PRED)
public class LanguageMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var language = commonMapper.readResourceDoc(source, Language.class);
    commonMapper.addMappedProperties(source, NOTE_PRED, language::addPartItem);
    commonMapper.addMappedProperties(source, DATE_PRED, language::addSameAsItem);
    destination.addLanguageItem(new LanguageField().language(language));
    return destination;
  }
}
