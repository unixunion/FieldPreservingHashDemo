package com.kegans.fphdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

/**
 * Hashing service
 *
 */
@Component
@RestController
@PropertySource("classpath:application.properties")
@Configuration
@Service
public class FPHService implements ApplicationListener {

    private Logger logger = LoggerFactory.getLogger(FPHService.class);

    @Autowired
    private Environment env;

    @Value("${fph.algo}")
    private String algorithm;

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
    public byte[] hash(@RequestBody String data) throws NoSuchAlgorithmException {
        FieldPreservingHash container = new FieldPreservingHash(data, algorithm);
        String response = container.hash();
        return response.getBytes();
    }

    @PostConstruct
    private void init() {
        logger.info("init");
    }

}
