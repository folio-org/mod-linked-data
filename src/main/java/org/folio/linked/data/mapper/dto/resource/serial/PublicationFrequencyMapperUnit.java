package org.folio.linked.data.mapper.dto.resource.serial;

import static org.folio.ld.dictionary.PredicateDictionary.PUBLICATION_FREQUENCY;
import static org.folio.ld.dictionary.PropertyDictionary.CODE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LINK;
import static org.folio.ld.dictionary.ResourceTypeDictionary.FREQUENCY;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.specific.PublicationFrequencyDictionary;
import org.folio.linked.data.domain.dto.InstanceResponse;
import org.folio.linked.data.domain.dto.PublicationFrequency;
import org.folio.linked.data.domain.dto.PublicationFrequencyResponse;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.instance.sub.InstanceSubResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = FREQUENCY, predicate = PUBLICATION_FREQUENCY, requestDto = PublicationFrequency.class)
public class PublicationFrequencyMapperUnit implements InstanceSubResourceMapperUnit {

  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof InstanceResponse instance) {
      var frequency = coreMapper.toDtoWithEdges(resourceToConvert, PublicationFrequencyResponse.class, false);
      frequency.setId(String.valueOf(resourceToConvert.getId()));
      instance.addPublicationFrequencyItem(frequency);
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var frequency = (PublicationFrequency) dto;
    var resource = new Resource()
      .setLabel(getFirstValue(frequency::getLabel))
      .addTypes(FREQUENCY)
      .setDoc(getDoc(frequency));
    resource.setId(hashService.hash(resource));
    return resource;
  }

  private JsonNode getDoc(PublicationFrequency dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LABEL, dto.getLabel());
    putProperty(map, LINK, dto.getLink());
    putProperty(map, CODE, linksToCodes(dto.getLink()));
    return map.isEmpty() ? null : coreMapper.toJson(map);
  }

  private List<String> linksToCodes(List<String> links) {
    return links.stream()
      .map(PublicationFrequencyDictionary::getCode)
      .flatMap(Optional::stream)
      .map(String::valueOf)
      .toList();
  }
}
