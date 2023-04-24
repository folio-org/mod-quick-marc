package org.folio.qm.service.impl;

import lombok.RequiredArgsConstructor;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksClient.InstanceLink;
import org.folio.qm.client.LinksClient.InstanceLinks;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.service.LinkingRulesService;
import org.folio.qm.service.LinksService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinksServiceImpl implements LinksService {

  private final LinksClient linksClient;
  private final LinkingRulesService linkingRulesService;

  @Override
  public void setRecordLinks(QuickMarcView qmRecord) {
    if (!verifyFormat(qmRecord)) {
      return;
    }

    var instanceLinksOptional = linksClient.fetchLinksByInstanceId(qmRecord.getExternalId());
    instanceLinksOptional.ifPresent(instanceLinks -> populateLinks(qmRecord, instanceLinks));
  }

  @Override
  public void updateRecordLinks(QuickMarcEdit qmRecord) {
    if (!verifyFormat(qmRecord)) {
      return;
    }

    var instanceLinks = extractLinks(qmRecord);
    linksClient.putLinksByInstanceId(qmRecord.getExternalId(), instanceLinks);
  }

  private boolean verifyFormat(BaseMarcRecord quickMarc) {
    return MarcFormat.BIBLIOGRAPHIC.equals(quickMarc.getMarcFormat());
  }

  private InstanceLinks extractLinks(QuickMarcEdit quickMarc) {
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

  private void populateLinks(QuickMarcView qmRecord, InstanceLinks instanceLinks) {
    var linkingRules = linkingRulesService.getLinkingRules();
    instanceLinks.getLinks().forEach(instanceLink ->
      linkingRules.stream()
        .filter(rule -> rule.getId().equals(instanceLink.getLinkingRuleId()))
        .findFirst().ifPresent(rule -> {
          var fields = qmRecord.getFields().stream()
            .filter(fieldItem -> rule.getBibField().equals(fieldItem.getTag()))
            .toList();
          if (fields.size() == 1) {
            populateLink(fields.get(0), instanceLink);
          } else {
            fields.stream()
              .filter(fieldItem -> instanceLink.getAuthorityId().equals(fieldItem.getAuthorityId()))
              .forEach(fieldItem -> populateLink(fieldItem, instanceLink));
          }
        })
    );
  }

  private void populateLink(FieldItem fieldItem, InstanceLink instanceLink) {
    fieldItem.setAuthorityId(instanceLink.getAuthorityId());
    fieldItem.setAuthorityNaturalId(instanceLink.getAuthorityNaturalId());
    fieldItem.setLinkingRuleId(instanceLink.getLinkingRuleId());
  }
}
