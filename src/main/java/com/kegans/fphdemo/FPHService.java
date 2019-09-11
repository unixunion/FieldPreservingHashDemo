package com.kegans.fphdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Hashing service
 *
 */
@Component
@RestController
@Service
public class FPHService implements ApplicationListener {

    private Logger logger = LoggerFactory.getLogger(FPHService.class);

    @Value("${fph.algo}")
    private String algorithm;

    @Value("${fph.delim}")
    private String delim;

    @Value("${fph.impl}")
    private String impl;

    @Value("${fpe.ctr}")
    private String ctr;

    // some holders
    String alphabet;
    MessageDigest md;
    FieldPreservingEncryption fieldPreservingEncryption;



    // on app mount, initialize
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            logger.info("starting up");
            try {
                md = MessageDigest.getInstance("SHA3-256");
                fieldPreservingEncryption = new FieldPreservingEncryption(ctr);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
            for (Provider provider : Security.getProviders()) {
                logger.debug("Provider: " + provider.getName());
                for (Provider.Service service : provider.getServices()) {
                    logger.debug("  Algorithm: " + service.getAlgorithm());
                }
            }

        }
    }


    // hash a payload
    @PostMapping("/hash")
    public byte[] hash(@RequestBody String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        FieldPreservingHash container = new FieldPreservingHash(data, delim, algorithm, alphabet);
        String response = container.hash();
        return response.getBytes();
    }


    // convert a byte to HEX
    public static String convertToHex(byte[] input) {
        StringBuilder sb = new StringBuilder(2*input.length);
        for(byte b : input){
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }


    // convert hex values to ASCII
    private static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }


    // encrypt a payload, with itself as the key
    @PostMapping("/crypt")
    public String crypt(@RequestBody String data) throws InvalidKeyException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] hash = md.digest(data.getBytes());
        SecretKey key = new SecretKeySpec(hash, 0, hash.length, "AES");
        fieldPreservingEncryption.getC().init(Cipher.ENCRYPT_MODE, key,  new IvParameterSpec(Arrays.copyOfRange(data.getBytes(), 0, 16)));

        byte[] tmp = new byte[data.length()];
        int i = 0;
        for (byte x : data.getBytes()) {
            byte[] tmpB = {x};
            tmp[i] = fieldPreservingEncryption.getC().update(tmpB)[0];
            i++;
        }

        return convertToHex(tmp);

//        byte[] encrypted = fieldPreservingEncryption.getC().doFinal(data.getBytes());
//        return convertToHex(encrypted);
    }


    // get characters from a unicode block
    public static Set<Character> findCharactersInUnicodeBlock(final Character.UnicodeBlock block) {
        final Set<Character> chars = new HashSet<Character>();
        for (int codePoint = Character.MIN_CODE_POINT; codePoint <= Character.MAX_CODE_POINT; codePoint++) {
            if (block == Character.UnicodeBlock.of(codePoint)) {
                chars.add((char) codePoint);
            }
        }
        return chars;
    }


    // initialize, loads the latin charactersets
    @PostConstruct
    private void init() throws UnsupportedEncodingException {
        logger.info("initializing the latin alphabet");
        Set<Character> latinChars = findCharactersInUnicodeBlock(Character.UnicodeBlock.BASIC_LATIN);

        findCharactersInUnicodeBlock(Character.UnicodeBlock.LATIN_EXTENDED_A).iterator().forEachRemaining(c -> {
            latinChars.add(c);
        });
        findCharactersInUnicodeBlock(Character.UnicodeBlock.LATIN_EXTENDED_B).iterator().forEachRemaining(c -> {
            latinChars.add(c);
        });
        findCharactersInUnicodeBlock(Character.UnicodeBlock.LATIN_EXTENDED_C).iterator().forEachRemaining(c -> {
            latinChars.add(c);
        });
        findCharactersInUnicodeBlock(Character.UnicodeBlock.LATIN_EXTENDED_D).iterator().forEachRemaining(c -> {
            latinChars.add(c);
        });
        findCharactersInUnicodeBlock(Character.UnicodeBlock.LATIN_EXTENDED_E).iterator().forEachRemaining(c -> {
            latinChars.add(c);
        });
        findCharactersInUnicodeBlock(Character.UnicodeBlock.LATIN_1_SUPPLEMENT).iterator().forEachRemaining(c -> {
            latinChars.add(c);
        });

        latinChars.iterator().forEachRemaining(c -> {
            if (Pattern.matches("\\p{IsLatin}", String.valueOf(c))) {
//                logger.info(String.valueOf(c));
                alphabet = alphabet + (char)c;
            }
        });
    }

}
