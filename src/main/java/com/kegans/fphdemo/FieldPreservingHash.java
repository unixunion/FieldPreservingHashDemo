package com.kegans.fphdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Pattern;


/**
 * This is a field preserving hashing function, which preserves digit positions
 * and spaces in the output format to match the input format.
 *
 * Implementation:
 *  1. Encrypt the data with the data itself as the key
 *  2. Convert the encrypted data to a Base64 encoded
 *  3. Get digits, spaces and chars from the Base64 encoded to match input style
 *  4. discard the rest of the encrypted bits
 *
 */
public class FieldPreservingHash {

    private Logger logger = LoggerFactory.getLogger(FieldPreservingHash.class);
    MessageDigest md = MessageDigest.getInstance("SHA3-256");

    SecretKey key;
    CipherOutputStream output;
    ByteArrayOutputStream outputStream;
    String data;

    int lastExtractedDigitIndex = 0;
    int lastAlphaIndex = 0;


    public FieldPreservingHash(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        this.data = data;
        byte[] hash = md.digest(data.getBytes());

        this.key = new SecretKeySpec(hash, 0, hash.length, "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);

        // byte output stream
        outputStream = new ByteArrayOutputStream();

        // a cipher outputstream to do the encryption
        output = new CipherOutputStream(outputStream, c);
    }


    // get Digit, extend encrypted data if no more digits.
    private char getDigit() throws IOException {
        for (int i=lastExtractedDigitIndex; i<getBase64EncodedValue().getBytes().length; i++) {
            if (Character.isDigit(getBase64EncodedValue().getBytes()[i])) {
                lastExtractedDigitIndex = i+1;
                return (char) getBase64EncodedValue().getBytes()[i];
            }
        }

        logger.debug("Out of digits, recursing to find more");
        output.write(this.data.getBytes());
        output.flush();
        return getDigit();

    }

    // get alpha char, extend data if no chars found
    private char getAlpha() throws IOException {
        for (int i=lastAlphaIndex; i<getBase64EncodedValue().getBytes().length; i++) {
            if (Character.isAlphabetic(getBase64EncodedValue().getBytes()[i])) {
                lastAlphaIndex = i+1;
                return (char) getBase64EncodedValue().getBytes()[i];
            }
        }

        logger.debug("Out of alpha chars, recursing to find more");
        output.write(this.data.getBytes());
        output.flush();
        return getAlpha();

    }


    // hash data
    public String hash() throws IOException {
        this.output.write(this.data.getBytes());
        this.output.flush();

        final String[] response = {new String()};

        this.data.chars().forEach(ch -> {

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
                logger.info("punctuation preserve");
                response[0] = response[0] + (char)ch;
            } else {
                if (Character.isAlphabetic(ch)) {
                    //logger.info("char: {}", (char)encodedBase64[0].getBytes()[0]);
                    try {
                        response[0] = response[0] + getAlpha();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("unable to get digits");
                    }
                }
            }
        });

        return response[0];

    }


    public String getBase64EncodedValue() {
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }


    public ByteArrayOutputStream getOutputStream() {
        return outputStream;
    }


}
