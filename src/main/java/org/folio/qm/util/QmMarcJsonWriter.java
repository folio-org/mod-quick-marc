package org.folio.qm.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcStreamWriter;
import org.marc4j.marc.Record;

public class QmMarcJsonWriter extends MarcJsonWriter implements AutoCloseable {

  public QmMarcJsonWriter(OutputStream os) {
    super(os);
  }

  @Override
  public void write(Record record) {
    var marcStreamWriter = new MarcStreamWriter(new ByteArrayOutputStream());
    marcStreamWriter.write(record);
    marcStreamWriter.close();
    super.write(record);
  }
}
