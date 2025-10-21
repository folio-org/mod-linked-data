package org.folio.linked.data.mapper.dto.resource.hub;

import static com.github.jknack.handlebars.internal.lang3.StringUtils.SPACE;
import static java.lang.String.join;
import static org.folio.ld.dictionary.PredicateDictionary.CONTRIBUTOR;
import static org.folio.ld.dictionary.PredicateDictionary.CREATOR;
import static org.folio.ld.dictionary.PredicateDictionary.TITLE;
import static org.folio.ld.dictionary.PropertyDictionary.LABEL;
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
    coreMapper.addOutgoingEdges(hub, WorkRequest.class, hubDto.getLanguages(), PredicateDictionary.LANGUAGE);

    hub.setDoc(getDoc(hubDto, hub));
    hub.setLabel(getLabel(hubDto, hub));
    hub.setIdAndRefreshEdges(hashService.hash(hub));
    return hub;
  }

  private JsonNode getDoc(HubRequest dto, Resource hubResource) {
    var map = new HashMap<String, List<String>>();
    putProperty(map, LABEL, List.of(getLabel(dto, hubResource)));
    return coreMapper.toJson(map);
  }

  private String getLabel(HubRequest dto, Resource hubResource) {
    var titleLabel = getFirstValue(() -> getPrimaryMainTitles(dto.getTitle()));
    return hubResource.getOutgoingEdges().stream()
      .filter(edge -> CREATOR.getUri().equals(edge.getPredicate().getUri()))
      .map(edge -> edge.getTarget().getLabel())
      .findFirst()
      .map(creatorLabel -> join(SPACE, creatorLabel, titleLabel))
      .orElse(titleLabel);
  }
}
