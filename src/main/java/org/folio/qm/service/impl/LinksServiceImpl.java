package org.folio.qm.service.impl;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksClient.InstanceLink;
import org.folio.qm.client.LinksClient.InstanceLinks;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.qm.service.LinksService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinksServiceImpl implements LinksService {

  private final LinksClient linksClient;

  @Override
  public void setRecordLinks(QuickMarc qmRecord) {
    if (!MarcFormat.BIBLIOGRAPHIC.equals(qmRecord.getMarcFormat())) {
      return;
    }

    var instanceLinksOptional = linksClient.fetchLinksByInstanceId(qmRecord.getExternalId());
    instanceLinksOptional.ifPresent(instanceLinks -> populateLinks(qmRecord, instanceLinks));
  }

  private void populateLinks(QuickMarc qmRecord, InstanceLinks instanceLinks) {
    instanceLinks.getLinks().forEach(instanceLink -> {
      var fields = qmRecord.getFields().stream()
        .filter(fieldItem -> instanceLink.getBibRecordTag().equals(fieldItem.getTag()))
        .collect(Collectors.toList());

      if (fields.size() == 1) {
        populateLink(fields.get(0), instanceLink);
      } else {
        fields.stream()
          .filter(fieldItem -> instanceLink.getAuthorityId().equals(fieldItem.getAuthorityId()))
          .forEach(fieldItem -> populateLink(fieldItem, instanceLink));
      }
    });
  }

  private void populateLink(FieldItem fieldItem, InstanceLink instanceLink) {
    fieldItem.setAuthorityId(instanceLink.getAuthorityId());
    fieldItem.setAuthorityControlledSubfields(instanceLink.getBibRecordSubfields());
  }
}
