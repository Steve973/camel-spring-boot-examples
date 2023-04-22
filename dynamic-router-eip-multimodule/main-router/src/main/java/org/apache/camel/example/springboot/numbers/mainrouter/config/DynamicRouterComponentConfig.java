/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.camel.example.springboot.numbers.mainrouter.config;

import org.apache.camel.CamelContext;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dynamicrouter.DynamicRouterControlMessage;
import org.apache.camel.example.springboot.numbers.common.model.ControlMessage;
import org.apache.camel.spi.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.camel.component.dynamicrouter.DynamicRouterConstants.COMPONENT_SCHEME;

/**
 * This configuration ingests the config properties in the application.yml file.
 * Sets up the Camel route that feeds the Dynamic Router.
 */
@Configuration
@EnableConfigurationProperties(MainRouterConfig.class)
public class DynamicRouterComponentConfig {

    protected static final Logger LOG = LoggerFactory.getLogger(DynamicRouterComponentConfig.class);

    /**
     * Holds the config exchange.properties.
     */
    private final MainRouterConfig mainRouterConfig;

    /**
     * The Camel context.
     */
    private final CamelContext camelContext;

    /**
     * Create this config with the config properties object.
     *
     * @param mainRouterConfig the config properties object
     * @param camelContext the Camel context
     */
    public DynamicRouterComponentConfig(final MainRouterConfig mainRouterConfig,
                                        final CamelContext camelContext) {
        this.mainRouterConfig = mainRouterConfig;
        this.camelContext = camelContext;
    }

    /**
     * Creates a simple route to allow a producer to send messages through
     * the dynamic router on the routing channel.
     */
    @Bean
    RouteBuilder numbersRouter() {
        return new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from(mainRouterConfig.commandEntrypoint())
//                        .to("log:command_endpoint?showHeaders=true&showBody=true&multiline=true")
                        .to(COMPONENT_SCHEME + ":" + mainRouterConfig.routingChannel() +
                                "?recipientMode=" + mainRouterConfig.recipientMode());
            }
        };
    }

    @Bean
    Processor predicateProcessor() {
        return exchange -> {
            ControlMessage cm = exchange.getIn().getBody(ControlMessage.class);
            String exLang = cm.getExpressionLanguage();
            String expression = "#" + cm.getPredicate();
            Language language = camelContext.resolveLanguage(exLang);
            Predicate predicate = language.createPredicate(expression);
            DynamicRouterControlMessage message = new DynamicRouterControlMessage.SubscribeMessageBuilder()
                    .id(cm.getSubscriberId())
                    .channel(cm.getRoutingChannel())
                    .endpointUri(cm.getConsumeUri())
                    .predicate(predicate)
                    .priority(cm.getSubscriptionPriority())
                    .build();
            exchange.getIn().setBody(message);
            LOG.info("\n##########\nProcessed subscription for ID: {}\n##########", cm.getSubscriberId());
        };
    }

    /**
     * Creates a simple route to allow dynamic routing participants to
     * subscribe or unsubscribe.
     */
    @Bean
    RouteBuilder subscriptionRouter(Processor predicateProcessor) {
        return new RouteBuilder(camelContext) {
            @Override
            public void configure() {
                from(mainRouterConfig.controlEntrypoint())
                        .unmarshal().protobuf(ControlMessage.getDefaultInstance())
                        .process(predicateProcessor)
                        .to("dynamic-router:control");
            }
        };
    }
}
