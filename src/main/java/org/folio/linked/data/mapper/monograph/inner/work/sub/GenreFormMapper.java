package org.folio.linked.data.mapper.monograph.inner.work.sub;

import static org.folio.linked.data.util.BibframeConstants.GENRE_FORM_PRED;
import static org.folio.linked.data.util.BibframeConstants.SAME_AS_PRED;
import static org.folio.linked.data.util.BibframeConstants.SOURCE_PRED;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.GenreForm;
import org.folio.linked.data.domain.dto.GenreFormField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(GENRE_FORM_PRED)
public class GenreFormMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var genreForm = readResourceDoc(objectMapper, source, GenreForm.class);
    addMappedProperties(objectMapper, source, SOURCE_PRED, genreForm::addSourceItem);
    addMappedProperties(objectMapper, source, SAME_AS_PRED, genreForm::addSameAsItem);
    destination.addGenreFormItem(new GenreFormField().genreForm(genreForm));
    return destination;
  }
}
