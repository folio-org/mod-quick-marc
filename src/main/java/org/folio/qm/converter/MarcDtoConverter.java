package org.folio.qm.converter;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.ParsedRecordDto;
import org.folio.qm.domain.dto.QuickMarc;

public interface MarcDtoConverter extends MarcConverter<ParsedRecordDto, QuickMarc, MarcFormat> {

}
