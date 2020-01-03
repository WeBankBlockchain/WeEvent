package com.webank.weevent.governance.utils;

import org.springframework.util.StringUtils;

/**
 * jwt tools class
 *
 */
public class JwtUtils {
    private static final String AUTHORIZATION_HEADER_PREFIX = "Authorization";

    /**
     * Get the original token
     * remove 'Bearer ' string
     *
     * @param authorizationHeader
     * @return
     */
    public static String getRawToken(String authorizationHeader) {
        return authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
    }

    /**
     * get token header
     *
     * @param rawToken
     * @return
     */
    public static String getTokenHeader(String rawToken) {
        return AUTHORIZATION_HEADER_PREFIX + rawToken;
    }

    /**
     * Verify authorization request header
     *
     * @param authorizationHeader
     * @return
     */
    public static boolean validate(String authorizationHeader) {
        return StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX);
    }

    /**
     * Get authorization header prefix
     *
     * @return
     */
    public static String getAuthorizationHeaderPrefix() {
        return AUTHORIZATION_HEADER_PREFIX;
    }
}