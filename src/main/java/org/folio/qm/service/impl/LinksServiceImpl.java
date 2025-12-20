package org.folio.qm.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksClient.InstanceLink;
import org.folio.qm.client.LinksClient.InstanceLinks;
import org.folio.qm.domain.dto.BaseMarcRecord;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcEdit;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.service.LinkingRulesService;
import org.folio.qm.service.LinksService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
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
    log.info("Updating links for QuickMarc record [instanceId: {}, linksCount: {}]",
      qmRecord.getExternalId(), instanceLinks.totalRecords());
    log.info("Links to be updated: {}", instanceLinks.links());
    linksClient.putLinksByInstanceId(qmRecord.getExternalId(), instanceLinks);
  }

  private boolean verifyFormat(BaseMarcRecord quickMarc) {
    return MarcFormat.BIBLIOGRAPHIC.equals(quickMarc.getMarcFormat());
  }

  private InstanceLinks extractLinks(QuickMarcEdit quickMarc) {
    var links = quickMarc.getFields().stream()
      .filter(fieldItem -> fieldItem.getLinkDetails() != null)
      .map(fieldItem -> new InstanceLink()
        .setInstanceId(quickMarc.getExternalId())
        .setAuthorityId(fieldItem.getLinkDetails().getAuthorityId())
        .setAuthorityNaturalId(fieldItem.getLinkDetails().getAuthorityNaturalId())
        .setLinkingRuleId(fieldItem.getLinkDetails().getLinkingRuleId()))
      .toList();

    return new InstanceLinks(links, links.size());
  }

  private void populateLinks(QuickMarcView qmRecord, InstanceLinks instanceLinks) {
    var linkingRules = linkingRulesService.getLinkingRules();
    instanceLinks.links().forEach(instanceLink ->
      linkingRules.stream()
        .filter(rule -> rule.getId().equals(instanceLink.getLinkingRuleId()))
        .findFirst().ifPresent(rule -> {
          var fields = qmRecord.getFields().stream()
            .filter(fieldItem -> rule.getBibField().equals(fieldItem.getTag()))
            .toList();
          if (fields.size() == 1) {
            populateLink(fields.getFirst(), instanceLink);
          } else {
            fields.stream()
              .filter(fieldItem -> instanceLink.getAuthorityId().equals(
                Optional.ofNullable(fieldItem.getLinkDetails())
                  .map(LinkDetails::getAuthorityId)
                  .orElse(null)))
              .forEach(fieldItem -> populateLink(fieldItem, instanceLink));
          }
        })
    );
  }

  private void populateLink(FieldItem fieldItem, InstanceLink instanceLink) {
    var linkDetails = fieldItem.getLinkDetails();
    linkDetails.setAuthorityId(instanceLink.getAuthorityId());
    linkDetails.setAuthorityNaturalId(instanceLink.getAuthorityNaturalId());
    linkDetails.setLinkingRuleId(instanceLink.getLinkingRuleId());
    linkDetails.setStatus(instanceLink.getStatus());
    linkDetails.setErrorCause(instanceLink.getErrorCause());
  }
}
