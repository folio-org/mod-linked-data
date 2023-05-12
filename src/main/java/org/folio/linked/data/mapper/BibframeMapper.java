package org.folio.linked.data.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.model.entity.Bibframe;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING, imports = {Bibframe.class, BibframeCreateRequest.class, BibframeResponse.class})
public interface BibframeMapper {

  Bibframe map(BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse map(Bibframe bibframe);

}
