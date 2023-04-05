/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.example.springboot.numbers.mainrouter.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.camel.component.dynamicrouter.DynamicRouterConstants.COMPONENT_SCHEME;

/**
 * This configuration ingests the config properties in the application.yml file.
 * Sets up the Camel route that feeds the Dynamic Router.
 */
@Configuration
public class DynamicRouterComponentConfig {

    /**
     * Holds the config properties.
     */
    private final MainRouterConfig mainRouterConfig;

    /**
     * Create this config with the config properties object.
     *
     * @param mainRouterConfig the config properties object
     */
    public DynamicRouterComponentConfig(final MainRouterConfig mainRouterConfig) {
        this.mainRouterConfig = mainRouterConfig;
    }

    /**
     * Creates a simple route to allow a producer to send messages through
     * the dynamic router on the routing channel.
     */
    @Bean
    RouteBuilder numbersRouter() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from(mainRouterConfig.messageEntrypoint())
                        .to("%s:%s?recipientMode=%s".formatted(
                                COMPONENT_SCHEME, mainRouterConfig.routingChannel(), mainRouterConfig.recipientMode()));
            }
        };
    }
}
