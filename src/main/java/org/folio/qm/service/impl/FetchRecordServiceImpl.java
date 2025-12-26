package org.folio.qm.service.impl;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.Record;
import org.folio.qm.client.UsersClient;
import org.folio.qm.converter.SourceRecordConverter;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.mapper.UserMapper;
import org.folio.qm.service.FetchRecordService;
import org.folio.qm.service.FieldProtectionSetterService;
import org.folio.qm.service.LinksService;
import org.folio.qm.service.storage.source.SourceRecordService;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class FetchRecordServiceImpl implements FetchRecordService {

  private final SourceRecordService sourceRecordService;
  private final FieldProtectionSetterService protectionSetterService;
  private final SourceRecordConverter sourceRecordConverter;
  private final LinksService linksService;
  private final UsersClient usersClient;
  private final UserMapper userMapper;

  @Override
  public QuickMarcView fetchByExternalId(UUID externalId) {
    log.debug("findByExternalId:: trying to find quickMarc by externalId: {}", externalId);
    var sourceRecord = sourceRecordService.getByExternalId(externalId);
    var quickMarc = sourceRecordConverter.convert(sourceRecord);
    protectionSetterService.applyFieldProtection(quickMarc);
    linksService.setRecordLinks(quickMarc);
    setUserInfo(quickMarc, sourceRecord);
    log.info("findByExternalId:: quickMarc loaded by externalId: {}", externalId);
    return quickMarc;
  }

  private void setUserInfo(QuickMarcView quickMarc, Record sourceRecord) {
    if (sourceRecord.getMetadata() != null && sourceRecord.getMetadata().getUpdatedByUserId() != null) {
      usersClient.fetchUserById(sourceRecord.getMetadata().getUpdatedByUserId())
        .ifPresent(userDto -> {
          var userInfo = userMapper.fromDto(userDto);
          Objects.requireNonNull(quickMarc).getUpdateInfo().setUpdatedBy(userInfo);
        });
    }
  }
}
