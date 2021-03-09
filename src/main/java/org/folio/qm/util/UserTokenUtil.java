package org.folio.qm.util;

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import static org.folio.qm.util.ErrorUtils.buildError;
import static org.folio.qm.util.ErrorUtils.ErrorType.INTERNAL;
import static org.folio.qm.util.JsonUtils.jsonToObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import org.folio.qm.domain.entity.UserInfo;
import org.folio.qm.exception.AuthorizationException;

@Log4j2
@UtilityClass
public class UserTokenUtil {

  private final String INVALID_TOKEN_MESSAGE = "X-Okapi-Token does not contain a userId";

  public static String getUserIdFromToken(String token) {
    return userInfoFromToken(token)
      .map(UserInfo::getUserId)
      .orElseThrow(() -> {
        log.error(INVALID_TOKEN_MESSAGE);
        throw new AuthorizationException(buildError(SC_UNAUTHORIZED, INTERNAL, INVALID_TOKEN_MESSAGE));
      });
  }

  private static Optional<UserInfo> userInfoFromToken(String token) {
    try {
      String[] split = token.split("\\.");
      return Optional.of(jsonToObject(getJson(split[1]), UserInfo.class));
    } catch (Exception var3) {
      return Optional.empty();
    }
  }

  private static String getJson(String strEncoded) {
    byte[] decodedBytes = Base64.getDecoder().decode(strEncoded);
    return new String(decodedBytes, StandardCharsets.UTF_8);
  }
}
