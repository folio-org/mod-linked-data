package org.folio.linked.data.mapper.resource.monograph.inner.work.sub.title;

import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.VARIANT_TITLE;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.VariantTitle;
import org.folio.linked.data.domain.dto.VariantTitleField;
import org.folio.linked.data.domain.dto.Work;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.mapper.resource.monograph.inner.work.sub.WorkSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = VARIANT_TITLE)
public class WorkVariantTitleMapperUnit implements WorkSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final NoteMapperUnit<VariantTitle> noteMapper;

  @Override
  public Work toDto(Resource source, Work destination) {
    var variantTitle = coreMapper.readResourceDoc(source, VariantTitle.class);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, variantTitle);
    destination.addTitleItem(new VariantTitleField().variantTitle(variantTitle));
    return destination;
  }

}
