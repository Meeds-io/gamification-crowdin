package io.meeds.gamification.crowdin.utils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;

import java.security.SecureRandom;

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
}
