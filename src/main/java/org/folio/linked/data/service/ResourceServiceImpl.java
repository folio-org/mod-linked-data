package org.folio.linked.data.service;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.BibframeConstants.PROFILES;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

  private static final String RESOURCE_WITH_GIVEN_ID = "Resource record with given id [";
  private static final String IS_NOT_FOUND = "] is not found";
  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "resourceHash");

  private final ResourceRepository resourceRepo;
  private final BibframeMapper bibframeMapper;

  @Override
  public BibframeResponse getBibframeById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() ->
        new NotFoundException(RESOURCE_WITH_GIVEN_ID + id + IS_NOT_FOUND));
    return bibframeMapper.map(resource);
  }

  @Override
  public BibframeShortInfoPage getBibframeShortInfoPage(Integer pageNumber, Integer pageSize) {
    if (isNull(pageNumber)) {
      pageNumber = DEFAULT_PAGE_NUMBER;
    }
    if (isNull(pageSize)) {
      pageSize = DEFAULT_PAGE_SIZE;
    }
    var page = resourceRepo.findResourcesByType(PROFILES,
        PageRequest.of(pageNumber, pageSize, DEFAULT_SORT));
    var pageOfDto = page.map(bibframeMapper::map);
    return bibframeMapper.map(pageOfDto);
  }

}
