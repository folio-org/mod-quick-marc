package org.folio.qm.converter;

import org.folio.qm.domain.dto.MarcFormat;
import org.folio.qm.domain.dto.QuickMarc;
import org.folio.rest.jaxrs.model.ParsedRecordDto;

public interface MarcDtoConverter extends MarcConverter<ParsedRecordDto, QuickMarc, MarcFormat> {

}
