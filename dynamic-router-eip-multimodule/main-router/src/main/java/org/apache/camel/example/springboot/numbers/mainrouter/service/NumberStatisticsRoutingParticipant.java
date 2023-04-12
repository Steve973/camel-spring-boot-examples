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

package org.apache.camel.example.springboot.numbers.mainrouter.service;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.camel.Consume;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.MessageTypes;
import org.apache.camel.example.springboot.numbers.common.service.RoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

@Service
public class NumberStatisticsRoutingParticipant extends RoutingParticipant {

    private final Map<String, Long> countsMap;

    public NumberStatisticsRoutingParticipant(
            @Value("${main-router.dynamic-router-component.control-entrypoint}") String subscribeUri,
            @Value("${main-router.dynamic-router-component.routing-channel}") String routingChannel,
            @Value("${main-router.stats-subscriber.predicate}") String predicate,
            @Value("${main-router.stats-subscriber.expression-language}") String expressionLanguage,
            @Value("${main-router.stats-subscriber.subscription-priority}") int subscriptionPriority,
            @Value("${main-router.stats-subscriber.consume-uri}") String consumeUri,
            @Value("${main-router.dynamic-router-component.command-entrypoint}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("stats", subscribeUri, routingChannel, subscriptionPriority,
                predicate, expressionLanguage, consumeUri, commandUri, producerTemplate);
        this.countsMap = new TreeMap<>();
    }

    public Map<String, Long> getCountsMap() {
        return countsMap;
    }

    /**
     * Send the generate numbers message with the boolean toggle value.
     */
    public String sendGenerateNumbersCommand(Map<String, String> params) {
        CommandMessage generateNumbersCommandMessage = CommandMessage.newBuilder()
                .setCommand(MessageTypes.GENERATE_NUMBERS_COMMAND)
                .putAllParams(params)
                .build();
        Exchange exchange = ExchangeBuilder.anExchange(producerTemplate.getCamelContext())
                .withHeader("command", MessageTypes.GENERATE_NUMBERS_COMMAND)
                .withBody(generateNumbersCommandMessage.toByteArray())
                .build();
        producerTemplate.send(commandUri, exchange);
        return "{\"status\": \"generate numbers command sent\"}";
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param bytes the serialized command message
     */
    @Consume(property = "consumeUri")
    public void consumeMessage(final byte[] bytes) throws InvalidProtocolBufferException {
        CommandMessage message = CommandMessage.parseFrom(bytes);
        message.getParamsMap().forEach((n, v) -> countsMap.put(n, Long.valueOf(v)));
    }
}
