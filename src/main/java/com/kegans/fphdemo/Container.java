package com.kegans.fphdemo;

import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;

public class Container {

    SecretKey key;
    CipherOutputStream output;
    ByteArrayOutputStream outputStream;

    public Container(SecretKey key) {
        this.key = key;
        this.output = output;
        this.outputStream = outputStream;
    }
}
