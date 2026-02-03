package org.folio.linked.data.mapper.dto.resource.hub;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
import static org.folio.ld.dictionary.PropertyDictionary.LANGUAGE;
import static org.folio.ld.dictionary.ResourceTypeDictionary.HUB;
import static org.folio.linked.data.util.ResourceUtils.getFirstValue;
import static org.folio.linked.data.util.ResourceUtils.getPrimaryMainTitles;
import static org.folio.linked.data.util.ResourceUtils.putProperty;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.ld.dictionary.PredicateDictionary;
import org.folio.linked.data.domain.dto.HubField;
import org.folio.linked.data.domain.dto.HubRequest;
import org.folio.linked.data.domain.dto.HubResponse;
import org.folio.linked.data.domain.dto.HubResponseField;
import org.folio.linked.data.domain.dto.Language;
import org.folio.linked.data.domain.dto.ResourceResponseDto;
import org.folio.linked.data.domain.dto.WorkRequest;
import org.folio.linked.data.mapper.dto.resource.base.CoreMapper;
import org.folio.linked.data.mapper.dto.resource.base.MapperUnit;
import org.folio.linked.data.mapper.dto.resource.common.TopResourceMapperUnit;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.service.profile.ResourceProfileLinkingService;
import org.folio.linked.data.service.resource.hash.HashService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@MapperUnit(type = HUB, requestDto = HubField.class)
public class HubMapperUnit extends TopResourceMapperUnit {
  private final ResourceProfileLinkingService resourceProfileService;
  private final CoreMapper coreMapper;
  private final HashService hashService;

  @Override
  public <P> P toDto(Resource resourceToConvert, P parentDto, ResourceMappingContext context) {
    if (parentDto instanceof ResourceResponseDto resourceDto) {
      var hub = coreMapper.toDtoWithEdges(resourceToConvert, HubResponse.class, false);
      hub.setId(String.valueOf(resourceToConvert.getId()));
      hub.setProfileId(resourceProfileService.resolveProfileId(resourceToConvert));
      resourceDto.setResource(new HubResponseField().hub(hub));
    }
    return parentDto;
  }

  @Override
  public Resource toEntity(Object dto, Resource parentEntity) {
    var hubDto = ((HubField) dto).getHub();
    var hub = new Resource().addTypes(HUB);

    coreMapper.addOutgoingEdges(hub, WorkRequest.class, hubDto.getTitle(), TITLE);
    coreMapper.addOutgoingEdges(hub, WorkRequest.class, hubDto.getCreatorReference(), CREATOR);
    coreMapper.addOutgoingEdges(hub, WorkRequest.class, hubDto.getContributorReference(), CONTRIBUTOR);
    coreMapper.addOutgoingEdges(hub, WorkRequest.class, standardLanguages(hubDto), PredicateDictionary.LANGUAGE);

    hub.setDoc(getDoc(hubDto));
    hub.setLabel(getLabel(hubDto));
    hub.setIdAndRefreshEdges(hashService.hash(hub));
    return hub;
  }

  private JsonNode getDoc(HubRequest dto) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LABEL, List.of(getLabel(dto)));
    putProperty(map, LANGUAGE, nonStandardLanguages(dto));
    return coreMapper.toJson(map);
  }

  private String getLabel(HubRequest dto) {
    return getFirstValue(() -> getPrimaryMainTitles(dto.getTitle()));
  }

  private List<Language> standardLanguages(HubRequest dto) {
    var languages = dto.getLanguages();
    if (languages == null) {
      return List.of();
    }
    return languages.stream()
      .filter(lang -> !isEmpty(lang.getLink()))
      .toList();
  }

  private List<String> nonStandardLanguages(HubRequest dto) {
    var languages = dto.getLanguages();
    if (languages == null) {
      return List.of();
    }
    return languages.stream()
      .filter(lang -> isEmpty(lang.getLink()))
      .flatMap(language -> language.getTerm().stream())
      .toList();
  }
}
