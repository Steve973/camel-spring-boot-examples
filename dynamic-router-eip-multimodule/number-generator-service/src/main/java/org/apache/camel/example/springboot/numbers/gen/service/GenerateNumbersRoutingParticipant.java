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

package org.apache.camel.example.springboot.numbers.gen.service;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.camel.*;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.service.RoutingParticipant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.apache.camel.example.springboot.numbers.common.model.MessageTypes.PROCESS_NUMBER_COMMAND;

@Service
public class GenerateNumbersRoutingParticipant extends RoutingParticipant {

    protected static final Logger LOG = LoggerFactory.getLogger(GenerateNumbersRoutingParticipant.class);

    private static final String PARAM_TYPE_LIMIT = "limit";

    public GenerateNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.predicate}") String predicate,
            @Value("${number-generator.expression-language}") String expressionLanguage,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("generateNumbers", subscribeUri, routingChannel, subscriptionPriority,
                predicate, expressionLanguage, consumeUri, commandUri, producerTemplate);
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param body the serialized command message
     */
    @Override
    @Consume(property = "consumeUri")
    public void consumeMessage(final byte[] body, @Header(value = "number") String number) throws InvalidProtocolBufferException {
        CommandMessage message = CommandMessage.parseFrom(body);
        Map<String, String> params = message.getParamsMap();
        int limit = Integer.parseInt(params.getOrDefault(PARAM_TYPE_LIMIT, "0"));
        generateNumbers(limit);
    }

    private final Function<Integer, Mono<String>> sendNumberMessage = (n) ->
            Mono.just(String.valueOf(n))
                    .map(number -> {
                        producerTemplate.send(
                                commandUri, ExchangeBuilder.anExchange(producerTemplate.getCamelContext())
                                        .withHeader("command", PROCESS_NUMBER_COMMAND)
                                        .withHeader("number", number)
                                        .withBody(String.format("n %s", number))
                                        .build());
                        return number;
                    });

    /**
     * When a command has been received to generate numbers, this will continuously generate
     * numbers and send them in a command to have recipients process the numbers.  It will
     * only stop when a limit (if any) is reached, or if a subsequent command instructs
     * number message generation to stop
     *
     * @param limit the count of numbers to produce (zero means Integer.MAX_VALUE)
     */
    protected void generateNumbers(int limit) {
        try {
            producerTemplate.start();
            LOG.info("Generating numbers from 1 to {}", limit);
            long begin = System.currentTimeMillis();
            Flux.fromStream(IntStream.rangeClosed(1, limit).boxed())
                    .flatMap(sendNumberMessage)
                    .then()
                    .block();
            LOG.info("Generated numbers in {}s", (System.currentTimeMillis() - begin) / 1000);
        } catch (Exception e) {
            LOG.warn("########## Exception when trying to send number messages", e);
        }
    }
}
