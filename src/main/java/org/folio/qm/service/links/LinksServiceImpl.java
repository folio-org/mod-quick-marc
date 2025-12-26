package org.folio.qm.service.links;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksClient.InstanceLink;
import org.folio.qm.client.LinksClient.InstanceLinks;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinksServiceImpl implements LinksService {

  private final LinksClient linksClient;
  private final LinkingRulesService linkingRulesService;

  @Override
  public void setRecordLinks(QuickMarcView qmRecord) {
    if (!verifyFormat(qmRecord.getMarcFormat())) {
      return;
    }

    var instanceLinksOptional = linksClient.fetchLinksByInstanceId(qmRecord.getExternalId());
    instanceLinksOptional.ifPresent(instanceLinks -> populateLinks(qmRecord, instanceLinks));
  }

  @Override
  public void updateRecordLinks(QuickMarcRecord qmRecord) {
    if (!verifyFormat(qmRecord.getMarcFormat())) {
      return;
    }

    var instanceLinks = extractLinks(qmRecord);
    linksClient.putLinksByInstanceId(qmRecord.getExternalId(), instanceLinks);
  }

  private boolean verifyFormat(MarcFormat marcFormat) {
    return MarcFormat.BIBLIOGRAPHIC.equals(marcFormat);
  }

  private InstanceLinks extractLinks(QuickMarcRecord qmRecord) {
    var links = qmRecord.getSource().getFields().stream()
      .filter(fieldItem -> fieldItem.getLinkDetails() != null)
      .map(fieldItem -> new InstanceLink()
        .setInstanceId(qmRecord.getExternalId())
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
