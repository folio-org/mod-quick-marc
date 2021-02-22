package org.folio.qm.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lombok.experimental.UtilityClass;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

@UtilityClass
public final class ZIPArchiver {

  public static String unzip(String zippedString) throws IOException {
    byte[] decoded = Base64.decodeBase64(zippedString);
    try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(decoded))) {
      return IOUtils.toString(gzip, StandardCharsets.UTF_8);
    }
  }

  public static String zip(String source) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try(GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
      gzip.write(source.getBytes());
      gzip.flush();
    }
    return Base64.encodeBase64String(baos.toByteArray());
  }
}

