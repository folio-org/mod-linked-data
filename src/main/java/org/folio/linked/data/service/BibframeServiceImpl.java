package org.folio.linked.data.service;

import static java.util.Objects.isNull;
import static org.folio.linked.data.util.BibframeConstants.INSTANCE;
import static org.folio.linked.data.util.BibframeConstants.ITEM;
import static org.folio.linked.data.util.BibframeConstants.MONOGRAPH;
import static org.folio.linked.data.util.BibframeConstants.WORK;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.folio.linked.data.domain.dto.BibframeCreateRequest;
import org.folio.linked.data.domain.dto.BibframeInstanceInner;
import org.folio.linked.data.domain.dto.BibframeItemInner;
import org.folio.linked.data.domain.dto.BibframeResponse;
import org.folio.linked.data.domain.dto.BibframeShortInfoPage;
import org.folio.linked.data.domain.dto.BibframeUpdateRequest;
import org.folio.linked.data.domain.dto.BibframeWorkInner;
import org.folio.linked.data.exception.NotFoundException;
import org.folio.linked.data.exception.NotSupportedException;
import org.folio.linked.data.mapper.BibframeMapper;
import org.folio.linked.data.model.entity.Bibframe;
import org.folio.linked.data.model.entity.Resource;
import org.folio.linked.data.model.entity.ResourceType;
import org.folio.linked.data.repo.BibframeRepository;
import org.folio.linked.data.repo.ResourceTypeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BibframeServiceImpl implements BibframeService {

  private static final String RECORD_WITH_GIVEN_SLUG = "Bibframe record with given id [";
  private static final String DEFAULT_PROFILE_NOT_FOUND = "Default profile [";
  private static final String IS_NOT_FOUND = "] is not found";
  private static final String ONLY_MONOGRAPH_SUPPORTED = "Only Monograph profile is supported";


  private static final int DEFAULT_PAGE_NUMBER = 0;
  private static final int DEFAULT_PAGE_SIZE = 100;
  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "graphName");

  private final BibframeRepository bibframeRepo;
  private final ResourceTypeRepository resourceTypeRepo;
  private final MonographService monographService;
  private final BibframeMapper bibframeMapper;

  @Value("${bibframe.profile.default}")
  private String defaultProfile;

  @Override
  public BibframeResponse createBibframe(String okapiTenant, BibframeCreateRequest bibframeCreateRequest) {
    var bibframe = new Bibframe();
    bibframe.setConfiguration(bibframeMapper.toJson(bibframeCreateRequest));
    bibframe.setProfile(getDefaultProfile());
    var persisted = bibframeRepo.save(bibframe);
    var response = bibframeMapper.toBibframe(persisted.getConfiguration());
    response.setId(persisted.getId());
    return response;
  }

  private ResourceType getDefaultProfile() {
    return resourceTypeRepo.findBySimpleLabel(defaultProfile != null ? defaultProfile : MONOGRAPH)
        .orElseThrow(() -> new NotFoundException(DEFAULT_PROFILE_NOT_FOUND + defaultProfile + IS_NOT_FOUND));
  }

  @Override
  public BibframeResponse getBibframeById(String okapiTenant, Long id) {
    var bibframe = bibframeRepo.findById(id).orElseThrow(() ->
        new NotFoundException(RECORD_WITH_GIVEN_SLUG + id + IS_NOT_FOUND));

    BibframeResponse response;
    if (bibframe.getConfiguration() != null) {
      response = bibframeMapper.toBibframe(bibframe.getConfiguration());
    } else {
      response = new BibframeResponse();
      if (MONOGRAPH.equals(bibframe.getProfile().getSimpleLabel())) {
        addResources(response, bibframe.getResources(), monographService);
      } else {
        throw new NotSupportedException(ONLY_MONOGRAPH_SUPPORTED);
      }
    }
    response.setId(id);
    return response;
  }


  private void addResources(BibframeResponse response,
                            List<Resource> resources,
                            BibframeMappingService mapper) {
    var works = new ArrayList<BibframeWorkInner>();
    var instances = new ArrayList<BibframeInstanceInner>();
    var items = new ArrayList<BibframeItemInner>();

    for (var resource : resources) {
      switch (resource.getType().getSimpleLabel()) {
        case WORK -> works.add(mapper.toWork(resource));
        case INSTANCE -> instances.add(mapper.toInstance(resource));
        case ITEM -> items.add(mapper.toItem(resource));
        default -> {
        }
      }
    }

    response.setWork(works);
    response.setInstance(instances);
    response.setItem(items);
  }

  @Override
  public BibframeResponse updateBibframe(String okapiTenant, Long id, BibframeUpdateRequest bibframeUpdateRequest) {
    var bibframe = bibframeRepo.findById(id).orElseThrow(() ->
        new NotFoundException(RECORD_WITH_GIVEN_SLUG + id + IS_NOT_FOUND));
    bibframe.setConfiguration(bibframeMapper.toJson(bibframeUpdateRequest));
    var persisted = bibframeRepo.save(bibframe);
    var response = bibframeMapper.toBibframe(persisted.getConfiguration());
    response.setId(id);
    return response;
  }

  @Override
  public void deleteBibframe(String okapiTenant, Long id) {
    if (!bibframeRepo.existsById(id)) {
      throw new NotFoundException(RECORD_WITH_GIVEN_SLUG + id + IS_NOT_FOUND);
    }
    bibframeRepo.deleteById(id);
  }

  @Override
  public BibframeShortInfoPage getBibframeShortInfoPage(String okapiTenant, Integer pageNumber, Integer pageSize) {
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

}
