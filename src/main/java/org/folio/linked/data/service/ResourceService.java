package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;

public interface ResourceService {

  BibframeResponse createBibframe(BibframeRequest bibframeRequest);

  BibframeResponse getBibframeById(Long id);

  BibframeResponse updateBibframe(Long id, BibframeRequest bibframeUpdateRequest);

  void deleteBibframe(Long id);

  BibframeShortInfoPage getBibframeShortInfoPage(Integer pageNumber, Integer pageSize);

}
