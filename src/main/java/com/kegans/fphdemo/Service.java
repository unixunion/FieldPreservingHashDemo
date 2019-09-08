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
import java.io.IOException;
import java.security.*;

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



    @PostMapping("/hash")
    public byte[] hash(@RequestBody String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        FieldPreservingHash container = new FieldPreservingHash(data);
        String response = container.hash();
        return response.getBytes();
    }


}
