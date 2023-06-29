package org.folio.qm.service.impl;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.folio.qm.util.ErrorUtils.buildError;

import java.util.List;
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
import org.folio.qm.exception.ValidationException;
import org.folio.qm.service.LinkingRulesService;
import org.folio.qm.service.LinksService;
import org.folio.qm.util.ErrorUtils;
import org.folio.tenant.domain.dto.Error;
import org.folio.tenant.domain.dto.Errors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LinksServiceImpl implements LinksService {

  public static final String UPDATE_LINKS_FAILED_MESSAGE = "Failed to update links for marc record";

  private final LinksClient linksClient;
  private final LinkingRulesService linkingRulesService;

  @Override
  public void setRecordLinks(QuickMarcView qmRecord) {
    if (verifyFormat(qmRecord)) {
      var externalId = qmRecord.getExternalId();
      var instanceLinksOptional = linksClient.fetchLinksByInstanceId(externalId);
      instanceLinksOptional.ifPresent(instanceLinks -> populateLinks(qmRecord, instanceLinks));
    }
  }

  @Override
  public void updateRecordLinks(QuickMarcEdit qmRecord) {
    if (verifyFormat(qmRecord)) {
      var externalId = qmRecord.getExternalId();
      var instanceLinks = extractLinks(qmRecord);
      var updateRequest = linksClient.putLinksByInstanceId(externalId, instanceLinks);
      if (!updateRequest.getStatusCode().is2xxSuccessful()) {
        log.warn("updateRecordLinks:: updating links for qmRecord failed with status {}",
          updateRequest.getStatusCode());
        throw new ValidationException(extractUpdateError(updateRequest));
      }
    }
  }

  private boolean verifyFormat(BaseMarcRecord quickMarc) {
    return MarcFormat.BIBLIOGRAPHIC.equals(quickMarc.getMarcFormat());
  }

  private Error extractUpdateError(ResponseEntity<Errors> errorResponseEntity) {
    var body = errorResponseEntity.getBody();
    if (nonNull(body)) {
      var errors = body.getErrors();
      if (isNotEmpty(errors)) {
        return errors.get(0);
      }
    }
    return buildError(HttpStatus.BAD_REQUEST, ErrorUtils.ErrorType.INTERNAL, UPDATE_LINKS_FAILED_MESSAGE);
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

    instanceLinks.getLinks().forEach(instanceLink -> {
      var matchingRule = linkingRules.stream()
        .filter(rule -> rule.getId().equals(instanceLink.getLinkingRuleId()))
        .findFirst();

      matchingRule.ifPresent(rule -> {
        List<FieldItem> fields = qmRecord.getFields().stream()
          .filter(fieldItem -> rule.getBibField().equals(fieldItem.getTag()))
          .toList();

        fields.stream()
          .filter(fieldItem -> isMatchingAuthorityId(fieldItem, instanceLink))
          .forEach(fieldItem -> populateLink(fieldItem, instanceLink));
      });
    });
  }

  private void populateLink(FieldItem fieldItem, InstanceLink instanceLink) {
    var linkDetails = fieldItem.getLinkDetails();
    linkDetails.setAuthorityId(instanceLink.getAuthorityId());
    linkDetails.setAuthorityNaturalId(instanceLink.getAuthorityNaturalId());
    linkDetails.setLinkingRuleId(instanceLink.getLinkingRuleId());
    linkDetails.setStatus(instanceLink.getStatus());
    linkDetails.setErrorCause(instanceLink.getErrorCause());
  }

  private boolean isMatchingAuthorityId(FieldItem fieldItem, InstanceLink instanceLink) {
    return instanceLink.getAuthorityId().equals(
      Optional.ofNullable(fieldItem.getLinkDetails())
        .map(LinkDetails::getAuthorityId)
        .orElse(null)
    );
  }
}
