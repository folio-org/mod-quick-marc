package org.folio.qm.service.fetch;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.Record;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.dto.UpdateInfo;
import org.folio.qm.service.links.LinksService;
import org.folio.qm.service.storage.source.FieldProtectionSetterService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.folio.qm.service.storage.user.UserService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FetchRecordServiceImpl implements FetchRecordService {

  private final SourceRecordService sourceRecordService;
  private final FieldProtectionSetterService protectionSetterService;
  private final RecordConversionService conversionService;
  private final LinksService linksService;
  private final UserService userService;

  @Override
  public QuickMarcView fetchByExternalId(UUID externalId) {
    log.debug("findByExternalId:: trying to find source record by externalId: {}", externalId);
    var sourceRecord = sourceRecordService.getByExternalId(externalId);
    var quickMarc = conversionService.convert(sourceRecord, QuickMarcView.class);
    protectionSetterService.applyFieldProtection(quickMarc);
    linksService.setRecordLinks(quickMarc);
    setUserInfo(quickMarc, sourceRecord);
    log.info("findByExternalId:: quickMarc loaded by externalId: {}", externalId);
    return quickMarc;
  }

  private void setUserInfo(QuickMarcView quickMarc, Record sourceRecord) {
    if (sourceRecord.getMetadata() != null) {
      userService.fetchUser(UUID.fromString(sourceRecord.getMetadata().getUpdatedByUserId()))
        .ifPresent(userInfo -> {
          var updateInfo = quickMarc.getUpdateInfo();
          if (updateInfo == null) {
            updateInfo = new UpdateInfo();
            quickMarc.setUpdateInfo(updateInfo);
          }
          updateInfo.setUpdatedBy(userInfo);
        });
    }
  }
}
