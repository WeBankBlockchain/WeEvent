package com.webank.weevent.governance.utils;

import java.io.IOException;
import java.security.Security;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.governance.GovernanceApplication;
import com.webank.weevent.governance.entity.AccountEntity;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * jwt tools class
 */
@Slf4j
@Component
public class JwtUtils {
    public final static String AUTHORIZATION_HEADER_PREFIX = "Authorization";
    public final static int EXPIRE_TIME = 60 * 60 * 24 * 1000;

    public static boolean verifierToken(String token) throws IOException {
        try {
            JWTVerifier build = JWT.require(Algorithm.HMAC256(GovernanceApplication.getPrivateSecret())).build();
            build.verify(token);
            String property = Security.getProperty(token);
            if (property == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("token verification failed", e);
            return false;
        }

    }


    /**
     * @param username
     * @param expiration
     * @return token
     */
    public static String encodeToken(String username, int expiration) {
        try {
            JWTCreator.Builder builder = JWT.create();
            builder.withIssuer(username);
            // set expired date
            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, expiration);
            builder.withExpiresAt(now.getTime());
            return builder.sign(Algorithm.HMAC256(GovernanceApplication.getPrivateSecret()));
        } catch (JWTCreationException e) {
            log.error("create jwt token failed", e);
            return "";
        }
    }

    /**
     * decode VCEUser from token
     *
     * @param token token
     * @return VCEUser
     */
    public static AccountEntity decodeToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(GovernanceApplication.getPrivateSecret())).build();
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
        return Security.getProperty(request.getHeader(AUTHORIZATION_HEADER_PREFIX));
    }
}