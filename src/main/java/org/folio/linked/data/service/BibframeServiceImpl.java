package org.folio.linked.data.service;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.exception.AlreadyExistsException;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.repo.BibframeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BibframeServiceImpl implements BibframeService {

  private static final String RECORD_WITH_GIVEN_SLUG = "Bibframe record with given slug [";
  private static final String EXISTS_ALREADY = "] exists already";
  private static final String IS_NOT_FOUND = "] is not found";
  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "graphName");
  private final BibframeRepository bibframeRepo;
  private final BibframeMapper bibframeMapper;
  private final FileService fileService;
  private final ObjectMapper objectMapper;

  @Override
  public BibframeResponse createBibframe(BibframeCreateRequest bibframeCreateRequest, String okapiTenant) {
    var toPersist = bibframeMapper.map(bibframeCreateRequest);
    if (bibframeRepo.existsBySlug(toPersist.getSlug())) {
      throw new AlreadyExistsException(RECORD_WITH_GIVEN_SLUG + toPersist.getSlug() + EXISTS_ALREADY);
    }
    var persisted = save(toPersist, okapiTenant);
    return bibframeMapper.map(persisted);
  }

  @Override
  public BibframeResponse getBibframeBySlug(String slug, String okapiTenant) {
    var optionalBibframe = bibframeRepo.findBySlug(slug);
    return optionalBibframe.map(bibframe -> {
      bibframe.setConfiguration(getConfiguration(okapiTenant, bibframe.getSlug()));
      return bibframeMapper.map(bibframe);
    }).orElseThrow(() -> new NotFoundException(RECORD_WITH_GIVEN_SLUG + slug + IS_NOT_FOUND));
  }

  @SneakyThrows
  private JsonNode getConfiguration(String okapiTenant, String slug) {
    byte[] configJson = fileService.download(okapiTenant, slug);
    return objectMapper.readTree(configJson);
  }

  @Override
  public BibframeResponse updateBibframe(String slug, BibframeUpdateRequest bibframeUpdateRequest, String okapiTenant) {
    var toPersist = bibframeRepo.findBySlug(slug)
      .map(e -> bibframeMapper.update(e, bibframeUpdateRequest))
      .orElseThrow(() -> new NotFoundException(RECORD_WITH_GIVEN_SLUG + slug + IS_NOT_FOUND));
    var persisted = save(toPersist, okapiTenant);
    return bibframeMapper.map(persisted);
  }

  @Override
  public void deleteBibframe(String slug, String okapiTenant) {
    fileService.delete(okapiTenant, slug);
    if (bibframeRepo.deleteBySlug(slug) < 1) {
      throw new NotFoundException(RECORD_WITH_GIVEN_SLUG + slug + IS_NOT_FOUND);
    }
  }

  @Override
  public BibframeShortInfoPage getBibframeShortInfoPage(Integer pageNumber, Integer pageSize) {
    if (isNull(pageNumber)) {
      pageNumber = DEFAULT_PAGE_NUMBER;
    }
    if (isNull(pageSize)) {
      pageSize = DEFAULT_PAGE_SIZE;
    }
    var page = bibframeRepo.findAllBy(PageRequest.of(pageNumber, pageSize, DEFAULT_SORT));
    var pageOfDto = page.map(bibframeMapper::map);
    return bibframeMapper.map(pageOfDto);
  }

  private Bibframe save(Bibframe bibframe, String okapiTenant) {
    fileService.upload(okapiTenant, bibframe.getSlug(), bibframe.getConfiguration());
    return bibframeRepo.save(bibframe);
  }
}
