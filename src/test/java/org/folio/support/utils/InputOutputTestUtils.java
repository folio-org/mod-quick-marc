package org.folio.support.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

@UtilityClass
@Log4j2
public class InputOutputTestUtils {

  public static String readFile(String filePath) {
    log.info("Using mock datafile: {}", filePath);
    try {
      return FileUtils.readFileToString(getFile(filePath), StandardCharsets.UTF_8);
    } catch (IOException | URISyntaxException e) {
      return null;
    }
  }

  private static File getFile(String filename) throws URISyntaxException {
    return new File(Objects.requireNonNull(ApiTestUtils.class.getClassLoader().getResource(filename)).toURI());
  }
}
