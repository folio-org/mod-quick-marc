package org.folio.qm.service;

import org.folio.qm.domain.entity.JobProfile;
import org.folio.qm.domain.entity.JobProfileAction;
import org.folio.qm.domain.entity.RecordType;

public interface JobProfileService {

  JobProfile getJobProfile(RecordType recordType, JobProfileAction action);
}
