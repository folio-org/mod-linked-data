package org.folio.linked.data.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BibframeServiceImpl implements BibframeService {

  private final BibframeRepo bibframeRepo;
  private final MapperFacade mapperFacade;

  @Override
  public BibframeResponse createBibframe(String okapiTenant, BibframeCreateRequest bibframeCreateRequest) {
    Bibframe toPersist = mapperFacade.map(bibframeCreateRequest, Bibframe.class);
    Bibframe persisted = bibframeRepo.persist(toPersist);
    return mapperFacade.map(persisted, BibframeResponse.class);
  }

  @Override
  public BibframeResponse getBibframeById(UUID id) {
    Optional<Bibframe> optionalBibframe = bibframeRepo.read(id);
    return optionalBibframe.map(b -> mapperFacade.map(b, BibframeResponse.class))
      .orElseThrow(() -> new NotFoundException("Bibframe record with given id [" + id + "] is not found"));
  }
}
