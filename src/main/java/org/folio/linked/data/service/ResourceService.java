package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;

public interface ResourceService {

  BibframeResponse createBibframe(BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse getBibframeById(Long id);

  BibframeShortInfoPage getBibframeShortInfoPage(Integer pageNumber, Integer pageSize);

}
