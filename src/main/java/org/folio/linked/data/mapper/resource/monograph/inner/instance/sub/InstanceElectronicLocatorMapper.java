package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_URL;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance;
import org.folio.linked.data.domain.dto.Url;
import org.folio.linked.data.domain.dto.UrlField;
import org.folio.linked.data.mapper.resource.common.CommonMapper;
import org.folio.linked.data.mapper.resource.common.ResourceMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ResourceMapper(predicate = ELECTRONIC_LOCATOR_PRED, dtoClass = UrlField.class)
public class InstanceElectronicLocatorMapper implements InstanceSubResourceMapper {

  private final CommonMapper commonMapper;
  private final DictionaryService<ResourceType> resourceTypeService;
  private final InstanceNoteMapper noteMapper;

  @Override
  public Instance toDto(Resource source, Instance destination) {
    var url = commonMapper.toUrl(source);
    commonMapper.addMappedProperties(source, NOTE_PRED, url::addNoteItem);
    destination.addElectronicLocatorItem(new UrlField().url(url));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate) {
    var url = ((UrlField) dto).getUrl();
    var resource = new Resource();
    resource.setLabel(URL);
    resource.setType(resourceTypeService.get(URL));
    resource.setDoc(getDoc(url));
    commonMapper.mapResourceEdges(url.getNote(), resource, NOTE_PRED, noteMapper::toEntity);
    resource.setResourceHash(commonMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Url dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_URL, dto.getValue());
    return commonMapper.toJson(map);
  }
}
