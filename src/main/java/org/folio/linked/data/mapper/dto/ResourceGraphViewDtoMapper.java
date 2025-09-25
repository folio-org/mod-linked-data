package org.folio.linked.data.mapper.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.linked.data.domain.dto.ResourceGraphViewDto;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ResourceGraphViewDtoMapper {

  private final ObjectMapper objectMapper;

  public Optional<ResourceGraphViewDto> fromJson(String jsonGraph) {
    try {
      return Optional.of(objectMapper.readValue(jsonGraph, ResourceGraphViewDto.class));
    } catch (IOException e) {
      log.error("Failed to convert resource subgraph to Resource", e);
      return Optional.empty();
    }
  }
}
