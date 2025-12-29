package org.folio.qm.service.links;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.LinksClient;
import org.folio.qm.client.LinksClient.InstanceLink;
import org.folio.qm.client.LinksClient.InstanceLinks;
import org.folio.qm.domain.dto.FieldItem;
import org.folio.qm.domain.dto.LinkDetails;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.QuickMarcRecord;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LinksServiceImpl implements LinksService {

  private final LinksClient linksClient;
  private final LinkingRulesService linkingRulesService;

  @Override
  public void setRecordLinks(QuickMarcView qmRecord) {
    log.debug("setRecordLinks:: Setting record links for externalId: {}, format: {}", 
      qmRecord.getExternalId(), qmRecord.getMarcFormat());
    if (!verifyFormat(qmRecord.getMarcFormat())) {
      log.debug("setRecordLinks:: Skipping links for non-bibliographic format: {}", qmRecord.getMarcFormat());
      return;
    }

    var instanceLinksOptional = linksClient.fetchLinksByInstanceId(qmRecord.getExternalId());
    instanceLinksOptional.ifPresent(instanceLinks -> {
      log.debug("setRecordLinks:: Found {} links for externalId: {}", 
        instanceLinks.totalRecords(), qmRecord.getExternalId());
      populateLinks(qmRecord, instanceLinks);
    });
    log.info("setRecordLinks:: Record links set successfully for externalId: {}", qmRecord.getExternalId());
  }

  @Override
  public void updateRecordLinks(QuickMarcRecord qmRecord) {
    log.debug("updateRecordLinks:: Updating record links for externalId: {}, format: {}", 
      qmRecord.getExternalId(), qmRecord.getMarcFormat());
    if (!verifyFormat(qmRecord.getMarcFormat())) {
      log.debug("updateRecordLinks:: Skipping links update for non-bibliographic format: {}", qmRecord.getMarcFormat());
      return;
    }

    var instanceLinks = extractLinks(qmRecord);
    log.debug("updateRecordLinks:: Extracted {} links from record", instanceLinks.totalRecords());
    linksClient.putLinksByInstanceId(qmRecord.getExternalId(), instanceLinks);
    log.info("updateRecordLinks:: Record links updated successfully for externalId: {}", qmRecord.getExternalId());
  }

  private boolean verifyFormat(MarcFormat marcFormat) {
    return MarcFormat.BIBLIOGRAPHIC.equals(marcFormat);
  }

  private InstanceLinks extractLinks(QuickMarcRecord qmRecord) {
    log.trace("extractLinks:: Extracting links from QuickMarcRecord");
    var links = qmRecord.getSource().getFields().stream()
      .filter(fieldItem -> fieldItem.getLinkDetails() != null)
      .map(fieldItem -> new InstanceLink()
        .setInstanceId(qmRecord.getExternalId())
        .setAuthorityId(fieldItem.getLinkDetails().getAuthorityId())
        .setAuthorityNaturalId(fieldItem.getLinkDetails().getAuthorityNaturalId())
        .setLinkingRuleId(fieldItem.getLinkDetails().getLinkingRuleId()))
      .toList();

    log.trace("extractLinks:: Extracted {} links", links.size());
    return new InstanceLinks(links, links.size());
  }

  private void populateLinks(QuickMarcView qmRecord, InstanceLinks instanceLinks) {
    log.trace("populateLinks:: Populating links for {} instance links", instanceLinks.totalRecords());
    var linkingRules = linkingRulesService.getLinkingRules();
    log.trace("populateLinks:: Retrieved {} linking rules", linkingRules.size());
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
    log.trace("populateLinks:: Links populated successfully");
  }

  private void populateLink(FieldItem fieldItem, InstanceLink instanceLink) {
    var linkDetails = fieldItem.getLinkDetails();
    if (linkDetails == null) {
      linkDetails = new LinkDetails();
      fieldItem.setLinkDetails(linkDetails);
    }
    linkDetails.setAuthorityId(instanceLink.getAuthorityId());
    linkDetails.setAuthorityNaturalId(instanceLink.getAuthorityNaturalId());
    linkDetails.setLinkingRuleId(instanceLink.getLinkingRuleId());
    linkDetails.setStatus(instanceLink.getStatus());
    linkDetails.setErrorCause(instanceLink.getErrorCause());
  }
}
