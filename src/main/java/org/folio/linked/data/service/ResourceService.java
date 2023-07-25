package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;

public interface ResourceService {

  BibframeResponse createBibframe(BibframeRequest bibframeRequest, String tenant);

  BibframeResponse getBibframeById(Long id);

  BibframeResponse updateBibframe(Long id, BibframeRequest bibframeUpdateRequest, String tenant);

  void deleteBibframe(Long id, String tenant);

  BibframeShortInfoPage getBibframeShortInfoPage(Integer pageNumber, Integer pageSize);

}
