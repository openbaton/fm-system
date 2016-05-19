/*
* Copyright (c) 2015-2016 Fraunhofer FOKUS
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
@EntityScan(basePackages = {"org.openbaton.catalogue.mano.common.faultmanagement", "org.openbaton.catalogue.mano.common.monitoring"})
@ComponentScan(basePackages = "org.openbaton.faultmanagement")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
