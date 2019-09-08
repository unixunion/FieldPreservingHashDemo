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
 * Hashing service
 *
 */
@Component
@RestController
public class Service implements ApplicationListener {

    private Logger logger = LoggerFactory.getLogger(Service.class);

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ContextRefreshedEvent) {
            logger.info("starting up");

            for (Provider p : Security.getProviders()) {
                logger.info("provider: {}", p.getInfo());
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
