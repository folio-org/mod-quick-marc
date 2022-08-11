package org.folio.qm.service.impl;

import static org.folio.qm.config.CacheNames.JOB_PROFILE_CACHE;

import lombok.RequiredArgsConstructor;
import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;
import org.folio.qm.domain.repository.JobProfileRepository;
import org.folio.qm.exception.JobProfileNotFoundException;
import org.folio.qm.service.JobProfileService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobProfileServiceImpl implements JobProfileService {

  private final JobProfileRepository repository;

  @Override
  @Cacheable(cacheNames = JOB_PROFILE_CACHE,
    key = "@folioExecutionContext.tenantId + ':' + #recordType.name + '-' + #action.name")
  public JobProfile getJobProfile(RecordType recordType, JobProfileAction action) {
    return repository.findByProfileActionAndRecordType(action, recordType)
      .orElseThrow(() -> new JobProfileNotFoundException(recordType, action));
  }
}
