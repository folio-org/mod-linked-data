package org.folio.linked.data.mapper.resource.monograph.inner.instance.sub;

import static org.folio.linked.data.util.BibframeConstants.ELECTRONIC_LOCATOR_PRED;
import static org.folio.linked.data.util.BibframeConstants.NOTE;
import static org.folio.linked.data.util.BibframeConstants.NOTE_PRED;
import static org.folio.linked.data.util.BibframeConstants.URL;
import static org.folio.linked.data.util.BibframeConstants.URL_URL;
import static org.folio.linked.data.util.BibframeConstants.VALUE_PRED;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Instance2;
import org.folio.linked.data.domain.dto.Url2;
import org.folio.linked.data.domain.dto.UrlField2;
import org.folio.linked.data.mapper.resource.common.CoreMapper;
import org.folio.linked.data.mapper.resource.common.MapperUnit;
import org.folio.linked.data.mapper.resource.common.inner.sub.SubResourceMapper;
import org.folio.linked.data.mapper.resource.monograph.inner.common.NoteMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.service.dictionary.DictionaryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = URL, predicate = ELECTRONIC_LOCATOR_PRED, dtoClass = UrlField2.class)
public class InstanceElectronicLocator2MapperUnit implements Instance2SubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final DictionaryService<ResourceType> resourceTypeService;
  private final NoteMapperUnit<Url2> noteMapper;

  @Override
  public Instance2 toDto(Resource source, Instance2 destination) {
    var url = coreMapper.toUrl(source);
    coreMapper.addMappedResources(noteMapper, source, NOTE_PRED, url);
    destination.addElectronicLocatorItem(new UrlField2().url(url));
    return destination;
  }

  @Override
  public Resource toEntity(Object dto, String predicate, SubResourceMapper subResourceMapper) {
    var url = ((UrlField2) dto).getUrl();
    var resource = new Resource();
    resource.setLabel(URL_URL);
    resource.setType(resourceTypeService.get(URL_URL));
    resource.setDoc(getDoc(url));
    coreMapper.mapResourceEdges(url.getNote(), resource, NOTE, NOTE_PRED,
      (fieldDto, pred) -> noteMapper.toEntity(fieldDto, pred, null));
    resource.setResourceHash(coreMapper.hash(resource));
    return resource;
  }

  private JsonNode getDoc(Url2 dto) {
    var map = new HashMap<String, List<String>>();
    map.put(VALUE_PRED, dto.getValue());
    return coreMapper.toJson(map);
  }
}
