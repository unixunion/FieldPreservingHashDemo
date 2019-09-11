package com.kegans.fphdemo;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.hashids.Hashids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


/**
 * This is a field preserving hashing function, which preserves digit positions
 * and spaces in the output format to match the input format.
 *
 * Implementation:
 *  1. Hash the input
 *  2. Return hash, placing digits, spaces and chars aligned with input data
 *  3. Scan for additional digits / alphas by recursive hashing if needed.
 *
 */

public class FieldPreservingHash {

    private Logger logger = LoggerFactory.getLogger(FieldPreservingHash.class);
    MessageDigest md;

    SimpleMeterRegistry registry = new SimpleMeterRegistry ();
    Timer hashTimer = registry.timer("app.hash");


    byte[] delims;
    byte[] data;
    byte[] hash;
    String alphabet;

    int lastExtractedDigitIndex = 0;
    int lastAlphaIndex = 0;


    public FieldPreservingHash(String data, String delims, String algorithm, String alphabet) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        logger.info("instantiating with algo: {}", algorithm);
        this.md = MessageDigest.getInstance(algorithm);

        this.data = data.getBytes();
        this.delims = delims.getBytes();
        this.hash = md.digest(data.getBytes("UTF-8"));
        this.alphabet = alphabet;



//        HashCode hc = this.f.newHasher()
//                .putLong(9999)
//                .putString("xyzä", Charsets.UTF_8)
//                .hash();
//
//        logger.info("int {}", hc.asInt());
//        logger.info("long {}", hc.asLong());
//        logger.info("bytes  {}", new String(hc.asBytes()));
    }


    public String convertToHex(byte[] input) {
        StringBuilder sb = new StringBuilder(2*input.length);
        for(byte b : input){
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }



    private String shake128() {

        SHAKEDigest sd = new SHAKEDigest(128);
        sd.update(this.data, 0, this.data.length);
        byte[] hashedBytes = new byte[128 / 8];
        sd.doFinal(hashedBytes, 0);
        String sha3Hash = ByteUtils.toHexString(hashedBytes);
        return sha3Hash;

    }




    // hashid the data
    private String hashid() throws UnsupportedEncodingException {


        final String[] response = {""};

        StringTokenizer st = new StringTokenizer(new String(this.data), new String(this.delims));

        st.asIterator().forEachRemaining(token -> {
            Hashids hashids = new Hashids(new String(this.data), 1, this.alphabet);
            try {
                String id = hashids.encode(Long.parseLong(String.valueOf(token)));
                logger.info("hashed long: {}", id);
                response[0] = response[0] + id;
            } catch (Exception e) {
                String hash;

                try {
                    hash = convertToHex(md.digest(String.valueOf(token).getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e1) {
                    throw new RuntimeException("error converting hash to hex");
                }

                logger.info("digest: {}", hash);
                String id = hashids.encodeHex(hash);
                logger.info("hashed word: {}", id);
                response[0] = response[0] + id;

                //logger.warn("unable to process token: {}", token);
            }
        });

        return response[0];
    }


    // get Digit, extend hashed data if no more digits.
    private char getDigit() throws IOException {

        for (int i=lastExtractedDigitIndex; i<this.hash.length; i++) {
            if (Character.isDigit(this.hash[i])) {
                lastExtractedDigitIndex = i+1;
                return (char) this.hash[i];
            }
        }

        lastExtractedDigitIndex = this.hash.length-1;
        logger.warn("Out of digits, recursing to find more");
        logger.debug("lastExtractedDigitIndex {}", lastExtractedDigitIndex );
        logger.debug("old hash length: {}", this.hash.length );

        this.data = ArrayUtils.addAll(this.data, this.data);
        byte[] tmpHash = ArrayUtils.addAll(this.hash, md.digest(this.data));
        this.hash = tmpHash;

        logger.debug("new hash length: {}", this.hash.length );

        return getDigit();

    }

    // get alpha char, extend hashed data if no chars found
    private char getAlpha() {

        for (int i=lastAlphaIndex; i<this.hash.length; i++) {
//            if (Character.isAlphabetic(this.hash[i])) {
            if (Pattern.matches("\\p{IsLatin}", String.valueOf((char)this.hash[i]))) {
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


        try {
            this.hashid();
            logger.info("shake128: {}", this.shake128());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        final String[] response = {new String()};

        hashTimer.record(() -> {

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
//                    if (Character.isAlphabetic(ch)) {
                    if (Pattern.matches("\\p{IsLatin}", String.valueOf((char)ch))) {
                        response[0] = response[0] + getAlpha();
                    }
                }
            }


        });

        logger.info("time {}", hashTimer.totalTime(TimeUnit.MILLISECONDS));

        return response[0];

    }

}
