package com.webank.weevent.core.fabric.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import com.webank.weevent.core.config.FabricConfig;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/7
 */
public class FabricUser implements User {
    private FabricConfig fabricConfig;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public FabricUser(FabricConfig fabricConfig) {
        this.fabricConfig = fabricConfig;
    }

    @Override
    public String getName() {
        return this.fabricConfig.getOrgUserName();
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<String>();
    }

    @Override
    public String getAccount() {
        return "";
    }

    @Override
    public String getAffiliation() {
        return "";
    }

    @Override
    public String getMspId() {
        return this.fabricConfig.getMspId();
    }

    @Override
    public Enrollment getEnrollment() {
        return new Enrollment() {
            @Override
            public PrivateKey getKey() {
                try {
                    String privateKeyContent = new String(Files.readAllBytes(Paths.get(fabricConfig.getOrgUserKeyFile())));
                    privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
                    KeyFactory kf = KeyFactory.getInstance("ECDSA");
                    PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
                    return kf.generatePrivate(keySpecPKCS8);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public String getCert() {
                try {
                    return new String(Files.readAllBytes(Paths.get(fabricConfig.getOrgUserCertFile())));
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            }
        };
    }
}
