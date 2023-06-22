package org.folio.linked.data.mapper.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;
import static org.folio.linked.data.util.MappingUtil.addMappedProperties;
import static org.folio.linked.data.util.MappingUtil.readResourceDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.mapper.resource.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(VARIANT_TITLE)
public class WorkVariantTitleMapper implements WorkSubResourceMapper {

  private final ObjectMapper objectMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var variantTitle = readResourceDoc(objectMapper, source, VariantTitle.class);
    addMappedProperties(objectMapper, source, NOTE_PRED, variantTitle::addNoteItem);
    destination.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    return destination;
  }

}
