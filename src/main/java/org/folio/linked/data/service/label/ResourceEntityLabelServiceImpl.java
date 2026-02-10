package org.folio.linked.data.service.label;

import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.linked.data.util.ResourceUtils.addProperty;

import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.label.LabelGeneratorService;
import org.folio.linked.data.mapper.ResourceModelMapper;
import org.folio.linked.data.model.entity.Resource;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ResourceEntityLabelServiceImpl implements ResourceEntityLabelService {

  private static final int EDGES_DEPTH = 1;
  private final LabelGeneratorService labelGeneratorService;
  private final ResourceModelMapper resourceModelMapper;

  @Override
  public void assignLabelToResource(Resource resource) {
    var resourceModel = resourceModelMapper.toModel(resource, EDGES_DEPTH);
    var label = labelGeneratorService.getLabel(resourceModel);
    resource.setLabel(label);
    addProperty(resource, LABEL, label);
  }
}
