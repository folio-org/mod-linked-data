package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(type = VARIANT_TITLE)
public class WorkVariantTitleMapper implements WorkSubResourceMapper {

  private final CommonMapper commonMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var variantTitle = commonMapper.readResourceDoc(source, VariantTitle.class);
    commonMapper.addMappedProperties(source, NOTE_PRED, variantTitle::addNoteItem);
    destination.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    return destination;
  }

}
