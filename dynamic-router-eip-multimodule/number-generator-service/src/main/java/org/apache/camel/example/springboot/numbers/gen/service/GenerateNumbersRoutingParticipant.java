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
import org.apache.camel.Consume;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.service.RoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.camel.example.springboot.numbers.common.model.MessageTypes.PROCESS_NUMBER_COMMAND;

@Service
public class GenerateNumbersRoutingParticipant extends RoutingParticipant {

    private static final String PARAM_TYPE = "type";

    private static final String PARAM_TYPE_SEQUENTIAL = "sequential";

    private static final String PARAM_TYPE_RANDOM = "random";

    private static final String PARAM_TYPE_STOP = "stop";

    private static final String PARAM_TYPE_START = "start";

    private static final String PARAM_TYPE_LIMIT = "limit";

    /**
     * Flag so that subsequent commands can be used to halt number messages.
     */
    private final AtomicBoolean runFlag = new AtomicBoolean(false);

    /**
     * Instance for generating random numbers.
     */
    private final Random random = new Random();

    public GenerateNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.predicate}") String predicate,
            @Value("${number-generator.expression-language}") String expressionLanguage,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.generate-numbers-consume-uri}") String consumeUri,
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
     * @param bytes the serialized command message
     */
    @Override
    @Consume(property = "consumeUri")
    public void consumeMessage(final byte[] bytes) throws InvalidProtocolBufferException {
        CommandMessage message = CommandMessage.parseFrom(bytes);
        Map<String, String> params = message.getParamsMap();
        String type = params.getOrDefault(PARAM_TYPE, PARAM_TYPE_SEQUENTIAL);
        int start = Integer.parseInt(params.getOrDefault(PARAM_TYPE_START, "0"));
        int limit = Integer.parseInt(params.getOrDefault(PARAM_TYPE_LIMIT, "0"));
        switch (type) {
            case PARAM_TYPE_SEQUENTIAL, PARAM_TYPE_RANDOM -> {
                this.runFlag.set(true);
                generateNumbers(type, start, limit);
            }
            case PARAM_TYPE_STOP -> this.runFlag.set(false);
        }
    }

    /**
     * When a command has been received to generate numbers, this will continuously generate
     * numbers and send them in a command to have recipients process the numbers.  It will
     * only stop when a limit (if any) is reached, or if a subsequent command instructs
     * number message generation to stop
     *
     * @param type  type of numbers to create (sequential or random)
     * @param start the number to start with (when in sequential mode)
     * @param limit the count of numbers to produce (zero means Integer.MAX_VALUE)
     */
    protected void generateNumbers(String type, int start, int limit) {
        int current = start;
        int remaining = limit == 0 ? Integer.MAX_VALUE : limit;
        while (this.runFlag.get() && remaining > 0) {
            remaining--;
            int number = PARAM_TYPE_SEQUENTIAL.equals(type) ? current++ : random.nextInt(0, Integer.MAX_VALUE);
            CommandMessage processNumberCommand = CommandMessage.newBuilder()
                    .setCommand(PROCESS_NUMBER_COMMAND)
                    .putParams("number", String.valueOf(number))
                    .build();
            Exchange exchange = ExchangeBuilder.anExchange(producerTemplate.getCamelContext())
                    .withHeader("command", PROCESS_NUMBER_COMMAND)
                    .withHeader("number", number)
                    .withBody(processNumberCommand.toByteArray())
                    .build();
            producerTemplate.asyncSend(commandUri, exchange);
        }
    }
}
