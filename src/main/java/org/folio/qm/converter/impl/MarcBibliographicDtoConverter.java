package org.folio.qm.converter.impl;

import static org.folio.qm.converter.elements.Constants.BLVL;
import static org.folio.qm.converter.elements.Constants.BLVL_LEADER_POS;
import static org.folio.qm.converter.elements.Constants.DESC;
import static org.folio.qm.converter.elements.Constants.DESC_LEADER_POS;
import static org.folio.qm.converter.elements.Constants.ELVL;
import static org.folio.qm.converter.elements.Constants.ELVL_LEADER_POS;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_BEGIN_INDEX;
import static org.folio.qm.converter.elements.Constants.SPECIFIC_ELEMENTS_END_INDEX;
import static org.folio.qm.converter.elements.Constants.TYPE;
import static org.folio.qm.converter.elements.Constants.TYPE_OF_RECORD_LEADER_POS;

import java.util.LinkedHashMap;
import java.util.Map;

import org.folio.qm.converter.AbstractMarcDtoConverter;
import org.folio.qm.converter.elements.MaterialTypeConfiguration;
import org.folio.qm.domain.dto.MarcFormat;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public class MarcBibliographicDtoConverter extends AbstractMarcDtoConverter {

  @Override
  protected String getExternalId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getInstanceId();
  }

  @Override
  protected String getExternalHrId(ParsedRecordDto source) {
    return source.getExternalIdsHolder().getInstanceHrid();
  }

  @Override
  public MarcFormat supportedType() {
    return MarcFormat.BIBLIOGRAPHIC;
  }

  protected Map<String, Object> splitGeneralInformationControlField(String content, String leader) {
    var materialTypeConfiguration = MaterialTypeConfiguration.resolveContentType(leader);

    Map<String, Object> fieldItems = new LinkedHashMap<>();
    fieldItems.put(TYPE, leader.charAt(TYPE_OF_RECORD_LEADER_POS));
    fieldItems.put(BLVL, leader.charAt(BLVL_LEADER_POS));
    fieldItems.put(ELVL, leader.charAt(ELVL_LEADER_POS));
    fieldItems.put(DESC, leader.charAt(DESC_LEADER_POS));
    fieldItems.putAll(fillContentMap(MaterialTypeConfiguration.getCommonItems(), content, -1));
    fieldItems.putAll(fillContentMap(materialTypeConfiguration.getControlFieldItems(),
        content.substring(SPECIFIC_ELEMENTS_BEGIN_INDEX, SPECIFIC_ELEMENTS_END_INDEX), -1));
    return fieldItems;
  }
}
