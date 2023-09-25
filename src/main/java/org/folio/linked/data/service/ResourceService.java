package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.Bibframe2Request;
import org.folio.linked.data.domain.dto.Bibframe2Response;
import org.folio.linked.data.domain.dto.Bibframe2ShortInfoPage;
import org.folio.linked.data.domain.dto.ResourceDto;
import org.folio.linked.data.domain.dto.ResourceShortInfoPage;

public interface ResourceService {

  ResourceDto createResource(ResourceDto resourceRequest);

  ResourceDto getResourceById(Long id);

  ResourceDto updateResource(Long id, ResourceDto bibframeRequest);

  void deleteResource(Long id);

  ResourceShortInfoPage getResourceShortInfoPage(String type, Integer pageNumber, Integer pageSize);

  Bibframe2Response createBibframe2(Bibframe2Request bibframeRequest);

  Bibframe2Response getBibframe2ById(Long id);

  Bibframe2Response updateBibframe2(Long id, Bibframe2Request bibframeUpdateRequest);

  Bibframe2ShortInfoPage getBibframe2ShortInfoPage(String type, Integer pageNumber, Integer pageSize);

  Bibframe2ShortInfoPage getBibframe2ShortInfoPage(Integer pageNumber, Integer pageSize);
}
