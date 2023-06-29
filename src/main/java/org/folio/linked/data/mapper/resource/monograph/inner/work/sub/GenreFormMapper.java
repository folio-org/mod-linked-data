package org.folio.linked.data.mapper.resource.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.GENRE_FORM_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.GenreForm;
import org.folio.linked.data.domain.dto.GenreFormField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = GENRE_FORM_PRED)
public class GenreFormMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var genreForm = commonMapper.readResourceDoc(source, GenreForm.class);
    commonMapper.addMappedProperties(source, SOURCE_PRED, genreForm::addSourceItem);
    commonMapper.addMappedProperties(source, SAME_AS_PRED, genreForm::addSameAsItem);
    destination.addGenreFormItem(new GenreFormField().genreForm(genreForm));
    return destination;
  }
}
