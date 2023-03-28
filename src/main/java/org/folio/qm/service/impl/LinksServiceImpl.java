package org.folio.qm.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    if (!verifyFormat(qmRecord)) {
      return;
    }

    var instanceLinksOptional = linksClient.fetchLinksByInstanceId(qmRecord.getExternalId());
    instanceLinksOptional.ifPresent(instanceLinks -> populateLinks(qmRecord, instanceLinks));
  }

  @Override
  public void updateRecordLinks(QuickMarc qmRecord) {
    if (!verifyFormat(qmRecord)) {
      return;
    }

    var instanceLinks = extractLinks(qmRecord);
    linksClient.putLinksByInstanceId(qmRecord.getExternalId(), instanceLinks);
  }

  private boolean verifyFormat(QuickMarc quickMarc) {
    return MarcFormat.BIBLIOGRAPHIC.equals(quickMarc.getMarcFormat());
  }

  private InstanceLinks extractLinks(QuickMarc quickMarc) {
    var links = quickMarc.getFields().stream()
      .filter(fieldItem -> fieldItem.getAuthorityId() != null)
      .map(fieldItem -> new InstanceLink()
        .setInstanceId(quickMarc.getExternalId())
        .setAuthorityId(fieldItem.getAuthorityId())
        .setAuthorityNaturalId(fieldItem.getAuthorityNaturalId())
        .setLinkingRuleId(fieldItem.getLinkingRuleId()))
      .toList();

    return new InstanceLinks(links, links.size());
  }

  private void populateLinks(QuickMarc qmRecord, InstanceLinks instanceLinks) {
    instanceLinks.getLinks().forEach(instanceLink -> {
      var fieldsWithAuthorityId = qmRecord.getFields().stream()
        .filter(fieldItem -> fieldItem.getAuthorityId() != null).toList();
      var fields = fieldsWithAuthorityId.stream()
        .filter(fieldItem -> instanceLink.getAuthorityId().equals(fieldItem.getAuthorityId()))
        .toList();
      if (!fields.isEmpty()) {
        fields.stream()
          .filter(fieldItem -> instanceLink.getAuthorityId().equals(fieldItem.getAuthorityId()))
          .forEach(fieldItem -> populateLink(fieldItem, instanceLink));
      }

      setLinkingRules(fieldsWithAuthorityId);
    });
    qmRecord.setFields(new ArrayList<>(qmRecord.getFields()));
    qmRecord.getFields()
      .sort(Comparator.comparing(FieldItem::getLinkingRuleId, Comparator.nullsLast(Comparator.naturalOrder())));
  }

  private void setLinkingRules(List<FieldItem> fieldsWithAuthorityId) {
    var linkingRules = linksClient.fetchLinkingRules();
    fieldsWithAuthorityId.forEach(fieldItem ->
      linkingRules.stream()
        .filter(l -> l.getBibField().equals(fieldItem.getTag()))
        .findFirst()
        .ifPresent(l -> fieldItem.setLinkingRuleId(l.getId())));
  }

  private void populateLink(FieldItem fieldItem, InstanceLink instanceLink) {
    fieldItem.setAuthorityId(instanceLink.getAuthorityId());
    fieldItem.setAuthorityNaturalId(instanceLink.getAuthorityNaturalId());
    fieldItem.setLinkingRuleId(instanceLink.getLinkingRuleId());
  }
}
