package org.folio.qm.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

@UtilityClass
@Log4j2
public class IOTestUtils {

  @SneakyThrows
  public static String readFile(String filePath) {
    log.info("Using mock datafile: {}", filePath);
    return FileUtils.readFileToString(getFile(filePath), StandardCharsets.UTF_8);
  }

  private static File getFile(String filename) throws URISyntaxException {
    return new File(Objects.requireNonNull(APITestUtils.class.getClassLoader().getResource(filename)).toURI());
  }
}
