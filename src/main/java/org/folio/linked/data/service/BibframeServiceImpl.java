package org.folio.linked.data.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BibframeServiceImpl implements BibframeService {

  private final BibframeRepository bibframeRepo;
  private final BibframeMapper bibframeMapper;

  @Override
  public BibframeResponse createBibframe(String okapiTenant, BibframeRequest bibframeRequest) {
    Bibframe toPersist = bibframeMapper.map(bibframeRequest);
    if (bibframeRepo.existsBySlug(toPersist.getSlug())) {
      throw new AlreadyExistsException("Bibframe record with given slug [" + toPersist.getSlug() + "] exists already");
    }
    Bibframe persisted = bibframeRepo.save(toPersist);
    return bibframeMapper.map(persisted);
  }

  @Override
  public BibframeResponse getBibframeBySlug(String okapiTenant, String slug) {
    Optional<Bibframe> optionalBibframe = bibframeRepo.findBySlug(slug);
    return optionalBibframe.map(bibframeMapper::map)
      .orElseThrow(() -> new NotFoundException("Bibframe record with given slug [" + slug + "] is not found"));
  }
}
