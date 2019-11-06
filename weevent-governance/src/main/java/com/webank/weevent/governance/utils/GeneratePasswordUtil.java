package com.webank.weevent.governance.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class GeneratePasswordUtil {

    public static String generatePassword() throws NoSuchAlgorithmException {
        String[] pswdStr = { "qwertyuiopasdfghjklzxcvbnm", "QWERTYUIOPASDFGHJKLZXCVBNM", "0123456789",
                "~!@#$%^&*()_+{}|<>?:{}" };
        SecureRandom instance = SecureRandom.getInstance("SHA1PRNG");
        int pswdLen = 6;
        String pswd = " ";
        char[] chs = new char[pswdLen];
        for (int i = 0; i < pswdStr.length; i++) {
            int idx = (int) (instance.nextDouble() * pswdStr[i].length());
            chs[i] = pswdStr[i].charAt(idx);
        }

        for (int i = pswdStr.length; i < pswdLen; i++) {
            int arrIdx = (int) (instance.nextDouble() * pswdStr.length);
            int strIdx = (int) (instance.nextDouble() * pswdStr[arrIdx].length());
            chs[i] = pswdStr[arrIdx].charAt(strIdx);
        }

        for (int i = 0; i < 1000; i++) {
            int idx1 = (int) (instance.nextDouble() * chs.length);
            int idx2 = (int) (instance.nextDouble() * chs.length);
            if (idx1 == idx2) {
                continue;
            }

            char tempChar = chs[idx1];
            chs[idx1] = chs[idx2];
            chs[idx2] = tempChar;
        }

        pswd = new String(chs);
        return pswd;
    }
}
