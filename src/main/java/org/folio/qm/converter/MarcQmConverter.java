package org.folio.qm.converter;

import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public interface MarcQmConverter extends MarcConverter<QuickMarc, ParsedRecordDto, ParsedRecordDto.RecordType> {

}
