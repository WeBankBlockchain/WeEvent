/*
 *  Copyright 2016 DTCC, Fujitsu Australia Software Technology - All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.webank.weevent.broker.fabric.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.KeyFactory;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

public class FabricUser implements User {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public String getName() {
        return FabricDeployContractUtil.fabricConfig.getOrgUserName();
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
        return FabricDeployContractUtil.fabricConfig.getMspId();
    }

    @Override
    public Enrollment getEnrollment() {
        return new Enrollment() {
            @Override
            public PrivateKey getKey() {
                try {
                    String privateKeyContent = new String(Files.readAllBytes(Paths.get(FabricDeployContractUtil.fabricConfig.getOrgUserKeyFile())));
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
                    return new String(Files.readAllBytes(Paths.get(FabricDeployContractUtil.fabricConfig.getOrgUserCertFile())));
                } catch (IOException e) {
                    e.printStackTrace();
                    return "";
                }
            }
        };
    }
}
