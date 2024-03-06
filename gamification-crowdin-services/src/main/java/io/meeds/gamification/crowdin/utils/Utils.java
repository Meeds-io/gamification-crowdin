package io.meeds.gamification.crowdin.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Map;

public class Utils {
    private static final Log LOG                                        = ExoLogger.getLogger(Utils.class);
    public static final String  CONNECTOR_NAME                             = "crowdin";
    public static final String  PROJECT_ID                            = "projectId";
    public static final String  CROWDIN_CONNECTION_ERROR                    = "crowdin.connectionError";
    public static final String  AUTHORIZATION                              = "Authorization";
    public static final String  TOKEN                                      = "Bearer ";
    public static final String  CROWDIN_API_URL                             = "https://api.crowdin.com/api/v2";
    public static final String  PROJECTS                              = "/projects/";
    public static final String  WEBHOOKS                              = "/webhooks/";
    public static final String  AUTHORIZED_TO_ACCESS_CROWDIN_HOOKS         = "The user is not authorized to access crowdin Hooks";

    public static final String    GAMIFICATION_GENERIC_EVENT = "exo.gamification.generic.action";

    public static final String    GAMIFICATION_CANCEL_EVENT  = "gamification.cancel.event.action";

    public static String encode(String token) {
        try {
            CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
            return codecInitializer.getCodec().encode(token);
        } catch (TokenServiceInitializationException e) {
            LOG.warn("Error when encoding token", e);
            return null;
        }
    }

    public static String decode(String encryptedToken) {
        try {
            CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
            return codecInitializer.getCodec().decode(encryptedToken);
        } catch (TokenServiceInitializationException e) {
            LOG.warn("Error when decoding token", e);
            return null;
        }
    }

    public static String generateRandomSecret(int length) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder word = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char randomChar;
            if (secureRandom.nextBoolean()) {
                randomChar = (char) (secureRandom.nextInt(26) + 'A');
            } else {
                randomChar = (char) (secureRandom.nextInt(26) + 'a');
            }
            word.append(randomChar);
        }
        return word.toString();
    }
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonStringToMap(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            throw new IllegalStateException("Error converting JSON string to map: " + jsonString, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static String extractSubItem(Map<String, Object> map, String... keys) {
        Object currentObject = map;
        for (String key : keys) {
            if (currentObject instanceof Map) {
                currentObject = ((Map<String, Object>) currentObject).get(key);
            } else {
                return null;
            }
        }
        return currentObject != null ? currentObject.toString() : null;
    }

    public static boolean verifyWebhookSecret(String bearerToken, String expected) {
        if (bearerToken == null) {
            LOG.warn("Bearer token is required to verify Crowdin webhook secret");
            return false;
        }
        if (expected == null) {
            LOG.warn("Expected string is required to verify Crowdin webhook secret");
            return false;
        }
        return expected.equals(bearerToken);
    }
}
