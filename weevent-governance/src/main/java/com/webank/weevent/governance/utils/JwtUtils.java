package com.webank.weevent.governance.utils;

import java.security.Security;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.governance.entity.AccountEntity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;

/**
 * jwt tools class
 */
@Slf4j
public class JwtUtils {
    public static final String AUTHORIZATION_HEADER_PREFIX = "Authorization";
    public static final String PRIVATE_SECRET = "PrivateSecret";

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

    public static String createToken(Authentication authResult) {
        return Jwts.builder().setSubject(((User) authResult.getPrincipal()).getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                .signWith(SignatureAlgorithm.HS256, PRIVATE_SECRET).compact();

    }

    public static String verifierToken(String token) {
        JWTVerifier build = JWT.require(Algorithm.HMAC256(PRIVATE_SECRET)).build();
        DecodedJWT jwt = build.verify(token);
        // check expired date
        if (Calendar.getInstance().getTime().after(jwt.getExpiresAt())) {
            log.error("expired token at {}", jwt.getExpiresAt());
            return null;
        }
        return null;
    }


    /**
     * @param username
     * @param secret
     * @param expiration
     * @return token
     */
    public static String encodeToken(String username, String secret, int expiration) {
        try {
            JWTCreator.Builder builder = JWT.create();
            builder.withIssuer(username);
            // set expired date
            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, expiration);
            builder.withExpiresAt(now.getTime());

            return builder.sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException e) {
            log.error("create jwt token failed", e);
            return "";
        }
    }

    /**
     * decode VCEUser from token
     *
     * @param token token
     * @param secret sign secret
     * @return VCEUser
     */
    public static AccountEntity decodeToken(String token, String secret) {
        try {
            // .withIssuer() ?
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            DecodedJWT jwt = verifier.verify(token);

            // check expired date
            if (Calendar.getInstance().getTime().after(jwt.getExpiresAt())) {
                log.error("expired token at {}", jwt.getExpiresAt());
                return null;
            }

            return new AccountEntity(jwt.getIssuer());
        } catch (JWTVerificationException e) {
            log.error("invalid jwt token", e);
            return null;
        }
    }


    public static String getAccountId(HttpServletRequest request) {
        return Security.getProperty(request.getHeader(JwtUtils.AUTHORIZATION_HEADER_PREFIX));
    }
}