package org.openbaton.faultmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by mob on 09.11.15.
 */
@SpringBootApplication
@EnableJpaRepositories("org.openbaton.faultmanagement.fc")
@EntityScan(basePackages ={"org.openbaton.catalogue.mano.common.faultmanagement","org.openbaton.catalogue.mano.common.monitoring"})
@ComponentScan(basePackages = "org.openbaton.faultmanagement")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
