package org.folio.linked.data.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BibframeServiceImpl implements BibframeService {

  private static final String RECORD_WITH_GIVEN_SLUG = "Bibframe record with given slug [";
  private static final String EXISTS_ALREADY = "] exists already";
  private static final String IS_NOT_FOUND = "] is not found";
  private final BibframeRepository bibframeRepo;
  private final BibframeMapper bibframeMapper;

  @Override
  public BibframeResponse createBibframe(String okapiTenant, BibframeCreateRequest bibframeCreateRequest) {
    Bibframe toPersist = bibframeMapper.map(bibframeCreateRequest);
    if (bibframeRepo.existsBySlug(toPersist.getSlug())) {
      throw new AlreadyExistsException(RECORD_WITH_GIVEN_SLUG + toPersist.getSlug() + EXISTS_ALREADY);
    }
    Bibframe persisted = bibframeRepo.save(toPersist);
    return bibframeMapper.map(persisted);
  }

  @Override
  public BibframeResponse getBibframeBySlug(String okapiTenant, String slug) {
    Optional<Bibframe> optionalBibframe = bibframeRepo.findBySlug(slug);
    return optionalBibframe.map(bibframeMapper::map)
      .orElseThrow(() -> new NotFoundException(RECORD_WITH_GIVEN_SLUG + slug + IS_NOT_FOUND));
  }

  @Override
  public BibframeResponse updateBibframe(String okapiTenant, String slug, BibframeUpdateRequest bibframeUpdateRequest) {
    Bibframe toPersist = bibframeRepo.findBySlug(slug)
      .map(e -> bibframeMapper.update(e, bibframeUpdateRequest))
      .orElseThrow(() -> new NotFoundException(RECORD_WITH_GIVEN_SLUG + slug + IS_NOT_FOUND));
    Bibframe persisted = bibframeRepo.save(toPersist);
    return bibframeMapper.map(persisted);
  }

  @Override
  public void deleteBibframe(String okapiTenant, String slug) {
    if (bibframeRepo.deleteBySlug(slug) < 1) {
      throw new NotFoundException(RECORD_WITH_GIVEN_SLUG + slug + IS_NOT_FOUND);
    }
  }
}
