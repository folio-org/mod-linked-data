package org.folio.linked.data.service.label;

import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.linked.data.util.ResourceUtils.addProperty;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.ld.dictionary.label.LabelGeneratorService;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ResourceEntityLabelServiceImpl implements ResourceEntityLabelService {

  private static final int OUTGOING_EDGES_DEPTH = 1;
  private static final int INCOMING_EDGES_DEPTH = 0;
  private final LabelGeneratorService labelGeneratorService;
  private final ResourceModelMapper resourceModelMapper;

  @Override
  public void assignLabelToResource(Resource resource) {
    var resourceModel = resourceModelMapper.toModel(resource, OUTGOING_EDGES_DEPTH, INCOMING_EDGES_DEPTH);
    var label = labelGeneratorService.getLabel(resourceModel);
    if (StringUtils.isBlank(label)) {
      return;
    }
    resource.setLabel(label);
    addProperty(resource, LABEL, label);
  }
}
