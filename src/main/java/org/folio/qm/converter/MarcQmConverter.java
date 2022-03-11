package org.folio.qm.converter;

import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;

public interface MarcQmConverter extends MarcConverter<QuickMarc, ParsedRecordDto, ParsedRecordDto.RecordTypeEnum> {

}
