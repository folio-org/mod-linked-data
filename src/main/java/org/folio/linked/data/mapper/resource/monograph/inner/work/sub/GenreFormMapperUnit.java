package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.GENRE_FORM_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.GenreForm;
import org.folio.linked.data.domain.dto.GenreFormField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(predicate = GENRE_FORM_PRED)
public class GenreFormMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var genreForm = coreMapper.readResourceDoc(source, GenreForm.class);
    coreMapper.addMappedProperties(source, SOURCE_PRED, genreForm::addSourceItem);
    coreMapper.addMappedProperties(source, SAME_AS_PRED, genreForm::addSameAsItem);
    destination.addGenreFormItem(new GenreFormField().genreForm(genreForm));
    return destination;
  }
}
