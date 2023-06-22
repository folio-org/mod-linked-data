package org.folio.linked.data.service;

import static java.util.Objects.isNull;

import lombok.RequiredArgsConstructor;
import org.folio.linked.data.configuration.properties.BibframeProperties;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.repo.ResourceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

  private static final String BIBFRAME_WITH_GIVEN_ID = "Bibframe record with given id [";
  private static final String IS_NOT_FOUND = "] is not found";
  private static final String BIBFRAME_PROFILE = "Bibframe profile [";
  private static final String IS_NOT_IN_THE_LIST_OF_SUPPORTED = "] is not in the list of supported: ";
  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "resourceHash");
  private final ResourceRepository resourceRepo;
  private final BibframeMapper bibframeMapper;
  private final BibframeProperties bibframeProperties;

  @Override
  public BibframeResponse createBibframe(BibframeCreateRequest bibframeCreateRequest) {
    if (!bibframeProperties.getProfiles().contains(bibframeCreateRequest.getProfile())) {
      throw new NotSupportedException(BIBFRAME_PROFILE + bibframeCreateRequest.getProfile()
        + IS_NOT_IN_THE_LIST_OF_SUPPORTED + String.join(", ", bibframeProperties.getProfiles()));
    }
    Resource persisted = resourceRepo.save(bibframeMapper.map(bibframeCreateRequest));
    return bibframeMapper.map(persisted);
  }

  @Override
  public BibframeResponse getBibframeById(Long id) {
    var resource = resourceRepo.findById(id).orElseThrow(() ->
      new NotFoundException(BIBFRAME_WITH_GIVEN_ID + id + IS_NOT_FOUND));
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
    var page = resourceRepo.findResourcesByType(bibframeProperties.getProfiles(),
      PageRequest.of(pageNumber, pageSize, DEFAULT_SORT));
    var pageOfDto = page.map(bibframeMapper::map);
    return bibframeMapper.map(pageOfDto);
  }

}
