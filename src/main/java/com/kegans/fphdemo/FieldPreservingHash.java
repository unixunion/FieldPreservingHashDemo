package com.kegans.fphdemo;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * This is a field preserving hashing function, which preserves digit positions
 * and spaces in the output format to match the input format.
 *
 * Implementation:
 *  1. Hash the input
 *  2. Hash digits, spaces and chars aligned with input data
 *  3. discard the rest of the hash
 *
 */
public class FieldPreservingHash {

    private Logger logger = LoggerFactory.getLogger(FieldPreservingHash.class);
    MessageDigest md = MessageDigest.getInstance("SHA-256");

    SimpleMeterRegistry registry = new SimpleMeterRegistry ();
    Timer timer = registry.timer("app.hash");

    byte[] data;
    byte[] hash;

    int lastExtractedDigitIndex = 0;
    int lastAlphaIndex = 0;


    public FieldPreservingHash(String data) throws NoSuchAlgorithmException {
        this.data = data.getBytes();
        this.hash = md.digest(data.getBytes());
    }


    // get Digit, extend encrypted data if no more digits.
    private char getDigit() throws IOException {

        for (int i=lastExtractedDigitIndex; i<this.hash.length; i++) {
            if (Character.isDigit(this.hash[i])) {
                lastExtractedDigitIndex = i+1;
                return (char) this.hash[i];
            }
        }

        lastExtractedDigitIndex = this.hash.length-1;
        logger.debug("Out of digits, recursing to find more");
        logger.debug("lastExtractedDigitIndex {}", lastExtractedDigitIndex );
        logger.debug("old hash length: {}", this.hash.length );

        this.data = ArrayUtils.addAll(this.data, this.data);
        byte[] tmpHash = ArrayUtils.addAll(this.hash, md.digest(this.data));
        this.hash = tmpHash;

        logger.debug("new hash length: {}", this.hash.length );

        return getDigit();

    }

    // get alpha char, extend data if no chars found
    private char getAlpha() {

        for (int i=lastAlphaIndex; i<this.hash.length; i++) {
            if (Character.isAlphabetic(this.hash[i])) {
                lastAlphaIndex = i+1;
                return (char) this.hash[i];
            }
        }

        logger.debug("Out of alpha chars, recursing to find more");


        this.data = ArrayUtils.addAll(this.data, this.data);
        byte[] tmpHash = ArrayUtils.addAll(this.hash, md.digest(this.data));
        this.hash = tmpHash;

        return getAlpha();

    }


    // hash data
    public String hash() {

        final String[] response = {new String()};

        timer.record(() -> {


            for (byte ch : this.data) {

                if (Character.isDigit(ch)) {
                    try {
                        response[0] = response[0] + getDigit();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("unable to get digits");
                    }
                } else if (Character.isSpaceChar(ch)) {
                    response[0] = response[0] + " ";
                } else if (Pattern.matches("\\p{Punct}", String.valueOf((char)ch))) {
                    logger.debug("punctuation preserve");
                    response[0] = response[0] + (char)ch;
                } else {
                    if (Character.isAlphabetic(ch)) {
                        response[0] = response[0] + getAlpha();
                    }
                }
            }


        });

        logger.info("time {}", timer.totalTime(TimeUnit.MILLISECONDS));

        return response[0];

    }




}
