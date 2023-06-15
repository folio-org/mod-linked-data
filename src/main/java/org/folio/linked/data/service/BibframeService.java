package org.folio.linked.data.service;

import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;

public interface BibframeService {

  BibframeResponse createBibframe(BibframeCreateRequest bibframeCreateRequest);

  BibframeResponse getBibframeBySlug(String slug);

  BibframeResponse updateBibframe(String slug, BibframeUpdateRequest bibframeUpdateRequest);

  void deleteBibframe(String slug);

  BibframeShortInfoPage getBibframeShortInfoPage(Integer pageNumber, Integer pageSize);
}
