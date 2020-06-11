package com.webank.weevent.file.inner;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.Key;

public class PemFile {
    private PemObject pemObject;

    public PemFile(Key key, String desc) {
        this.pemObject = new PemObject(desc, key.getEncoded());
    }

    public void write(String fileName) throws IOException {
        try (PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(fileName)))) {
            pemWriter.writeObject(this.pemObject);
        }

    }
}
