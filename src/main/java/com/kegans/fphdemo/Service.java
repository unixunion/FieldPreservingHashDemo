package com.kegans.fphdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * Sure. If you want a b-bit hash of the message m, then use the first b bits of
 * AES-CTR(SHA256(m)). That'll do the trick.
 *
 * In other words, compute SHA256(m) and treat the resulting 256-bit string as a
 * 256-bit AES key. Next, use AES in counter mode (with this key) to generate an
 * unending stream of pseudorandom bits. Take the first b bits from this stream,
 * and call it your hash. (Make sure you don't treat the IV as part of this stream,
 * as the IV won't be pseudorandom. You may need to manually remove the IV first
 * before taking the first b bits.)
 *
 * Security. This should be secure as long as b≥160 or so. In particular, a collision
 * attack is expected to take about 2min(b,256)/2 steps of computation, given our
 * current knowledge of AES and SHA256. So, as long as you don't choose a value of
 * b that is too small, you should be good. Choosing a value of b larger than 256
 * does not give you greater security against collisions, but that's irrelevant: the
 * level of security will already be way more than enough for any reasonable application,
 * so you're good.
 *
 *
 */
@Component
@RestController
public class Service implements ApplicationListener {

    private Logger logger = LoggerFactory.getLogger(Service.class);
    MessageDigest md;

    private char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,-_ ".toCharArray();
    private char[] punctuations = ".,:;".toCharArray();
    private char[] numbers = "0123456789".toCharArray();

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            logger.info("starting up");

            for (Provider p : Security.getProviders()) {
                logger.info("provider: {}", p.getInfo());
            }

            try {
                md = MessageDigest.getInstance("SHA3-256");

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    static boolean contains(char c, char[] array) {
        for (char x : array) {
            if (x == c) {
                return true;
            }
        }
        return false;
    }






    @PostMapping("/hash")
    public byte[] hash(@RequestBody String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        //logger.info("request to hash: {}", data);

        // tokenize on spaces, and punctuation
//        String[] tokenized = data.split("\\s*(=>|,|\\s)\\s*");




        // hash the input using sha256
        byte[] hash = md.digest(data.getBytes());
        //logger.info("hash: {}", new String(hash));

//
//        Container container = new Container(
//                new SecretKeySpec(hash, 0, hash.length, "AES")
//        );


        // use the hash as a secret key
        SecretKey key = new SecretKeySpec(hash, 0, hash.length, "AES");

        // instantiate cipher with AES and key
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);

        // byte output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // a cipher outputstream to do the encryption
        CipherOutputStream output = new CipherOutputStream(outputStream, c);
        output.write(data.getBytes());
        output.flush();
//        output.close();

        // flush the stream
        outputStream.flush();

        // encode the stream to base64
        final String[] encodedBase64 = {Base64.getEncoder().encodeToString(outputStream.toByteArray())};
        logger.info(encodedBase64[0]);

        // prepare the response string
//        final String[] responseString = new String[1];
        final String[] responseString = {""};

        // copy chars from the encoded encrypted data, to the result
        //  if they match our alphabet.


//        String finalEncodedBasde64 = encodedBase64[0];
        data.chars().forEach(ch -> {
            //logger.info("{}", (char)ch);
            if (Character.isDigit(ch)) {

                int fe_i = 0;
                for (byte ec : encodedBase64[0].getBytes()) {
                    if (Character.isDigit(ec)) {
                        logger.info("found encrypted digit: {} i: {}", (char)ec, fe_i);
                        responseString[0] = responseString[0] + (char)ec;
                        encodedBase64[0] = encodedBase64[0].substring(0, fe_i) + encodedBase64[0].substring(fe_i + 1);
                        break;
                    } else {
                        //logger.info("seeking for digit");
                        fe_i++;
                    }
                }

                if (fe_i>=encodedBase64[0].length()) {
                    logger.warn("out of numbers");
                    try {
                        output.write(data.getBytes());
                        output.flush();
                        encodedBase64[0] = Base64.getEncoder().encodeToString(outputStream.toByteArray());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else if (Character.isSpaceChar(ch)) {
                //logger.info("space");
                responseString[0] = responseString[0] + " ";
            } else {
                if (Character.isAlphabetic(ch)) {
                    //logger.info("char: {}", (char)encodedBase64[0].getBytes()[0]);
                    responseString[0] = responseString[0] + (char)encodedBase64[0].getBytes()[0];
                    encodedBase64[0] = encodedBase64[0].substring(1);
                }
            }
        });


//        for (byte x: data.getBytes()) {
//            int index = 0;
//            if (Character.isDigit((char)x)) {
//                // search and pull digits from encrypted
//                for (byte y: encodedBase64.getBytes()) {
//                    if (Character.isDigit(y)) {
//                        responseString = responseString + (char)y;
//                        encodedBase64 = encodedBase64.substring(0, index) + encodedBase64.substring(index + 1);
//                        break;
//                    }
//                    index++;
//                }
//            } else if (Character.isSpaceChar((char)x)) {
//                logger.info("space");
//                responseString = responseString + " ";
//            } else if (Character.isAlphabetic((char)x)) {
//                for (byte y: encodedBase64.getBytes()) {
//                    responseString = responseString + (char)y;
//                    encodedBase64 = encodedBase64.substring(1);
//                    break;
//                }
//            }
//
//        }


//        for (byte x: encodedBase64.getBytes()) {
//            if (responseString.length() < data.length()) {
//
//                if (Character.isDigit(data.charAt(pos))) {
//                    // select a digit
//
//
//                } else {
//
//                }
//
//                if (contains((char)x, alphabet)) {
//                    responseString = responseString + (char)x;
//                }
//            }
//            pos++;
//        }


        output.close();
        logger.info("hashed to: {}", responseString[0]);

        // write the bytes to the response
        return  responseString[0].getBytes();

    }


}
