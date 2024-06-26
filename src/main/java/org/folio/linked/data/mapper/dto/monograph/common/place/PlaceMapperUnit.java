package org.folio.linked.data.mapper.dto.monograph.common.place;

import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.PropertyDictionary.NAME;
import static org.folio.ld.dictionary.ResourceTypeDictionary.PLACE;
import static org.folio.linked.data.util.BibframeUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.Place;
import org.folio.linked.data.domain.dto.ProviderEventRequest;
import org.folio.linked.data.domain.dto.ProviderEventResponse;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.domain.dto.WorkResponse;
import org.folio.linked.data.mapper.dto.common.CoreMapper;
import org.folio.linked.data.mapper.dto.common.SingleResourceMapperUnit;
import org.folio.linked.data.mapper.dto.monograph.common.MarcCodeProvider;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.HashService;

@RequiredArgsConstructor
public abstract class PlaceMapperUnit implements SingleResourceMapperUnit, MarcCodeProvider {

  private static final String PLACE_LINK_PREFIX = "http://id.loc.gov/vocabulary/countries/";
  private static final Set<Class<?>> SUPPORTED_PARENTS = Set.of(
    ProviderEventRequest.class,
    ProviderEventResponse.class,
    WorkRequest.class,
    WorkResponse.class
  );

  protected final CoreMapper coreMapper;

  private final HashService hashService;

  protected abstract String getLabel(Place place);

  @Override
  public String getLinkPrefix() {
    return PLACE_LINK_PREFIX;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var place = (Place) dto;
    var resource = new Resource();
    resource.setLabel(getLabel(place));
    resource.addTypes(PLACE);
    resource.setDoc(getDoc(place));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  @Override
  public Set<Class<?>> supportedParents() {
    return SUPPORTED_PARENTS;
  }

  private JsonNode getDoc(Place dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, NAME, dto.getName());
    putProperty(map, CODE, getMarcCodes(dto.getLink()));
    putProperty(map, LABEL, dto.getLabel());
    putProperty(map, LINK, dto.getLink());
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }
}
