package fr.icdc.ebad.service.util;


import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

/**
 * Utility class for generating random Strings.
 */
public final class RandomUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int DEF_COUNT = 20;

    static {
        SECURE_RANDOM.nextBytes(new byte[64]);
    }

    private RandomUtil() {
    }

    private static String generateRandomAlphanumericString() {
        return RandomStringUtils.random(DEF_COUNT, 0, 0, true, true, null, SECURE_RANDOM);
    }


    /**
     * Generates an activation key.
     *
     * @return the generated activation key
     */
    public static String generateActivationKey() {
        return generateRandomAlphanumericString();
    }
}
