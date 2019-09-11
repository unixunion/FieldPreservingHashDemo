package com.kegans.fphdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;


public class FieldPreservingEncryption {

    private Logger logger = LoggerFactory.getLogger(FieldPreservingEncryption.class);

    String ctr;
    Cipher c;

    public FieldPreservingEncryption(String ctr) throws NoSuchPaddingException, NoSuchAlgorithmException {
        logger.info("instantiating: {}", ctr);
        c = Cipher.getInstance(ctr);
    }


    @PostConstruct
    private void init() {
        logger.info("loading config: {}", this.ctr);
        this.ctr = ctr;
    }

    // getter setters

    public Cipher getC() {
        return c;
    }

}
