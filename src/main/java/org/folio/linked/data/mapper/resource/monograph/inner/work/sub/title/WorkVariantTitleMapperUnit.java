package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE_2;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.VariantTitle2;
import org.folio.linked.data.domain.dto.VariantTitleField2;
import org.folio.linked.data.domain.dto.Work2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE_2)
public class WorkVariantTitleMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final NoteMapperUnit<VariantTitle2> noteMapper;

  @Override
  public Work2 toDto(Resource source, Work2 destination) {
    var variantTitle = coreMapper.readResourceDoc(source, VariantTitle2.class);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, variantTitle);
    destination.addTitleItem(new VariantTitleField2().variantTitle(variantTitle));
    return destination;
  }

}
