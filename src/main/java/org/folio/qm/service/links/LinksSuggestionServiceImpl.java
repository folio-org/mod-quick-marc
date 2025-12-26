package org.folio.qm.service.links;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.qm.client.LinksSuggestionsClient;
import org.folio.qm.convertion.RecordConversionService;
import org.folio.qm.domain.dto.AuthoritySearchParameter;
import org.folio.qm.domain.dto.QuickMarcView;
import org.folio.qm.domain.model.BaseSourceMarcRecord;
import org.folio.qm.domain.model.LinksSuggestions;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class LinksSuggestionServiceImpl implements LinksSuggestionService {

  private final LinksSuggestionsClient linksSuggestionsClient;
  private final RecordConversionService conversionService;

  @Override
  public QuickMarcView suggestLinks(QuickMarcView quickMarcView, AuthoritySearchParameter authoritySearchParameter,
                                    Boolean ignoreAutoLinkingEnabled) {
    log.debug("suggestLinks:: trying to suggest links");
    var baseSrsMarcRecord = conversionService.convert(quickMarcView, BaseSourceMarcRecord.class);
    var suggestionRequest = new LinksSuggestions().setRecords(List.of(baseSrsMarcRecord));
    var srsRecordsWithSuggestions = linksSuggestionsClient.postLinksSuggestions(
      suggestionRequest,
      authoritySearchParameter,
      ignoreAutoLinkingEnabled);
    if (isNotEmpty(srsRecordsWithSuggestions.getRecords())) {
      log.info("suggestLinks:: links was suggested");
      return conversionService.convert(srsRecordsWithSuggestions.getRecords().getFirst(), QuickMarcView.class);
    }
    return quickMarcView;
  }
}
