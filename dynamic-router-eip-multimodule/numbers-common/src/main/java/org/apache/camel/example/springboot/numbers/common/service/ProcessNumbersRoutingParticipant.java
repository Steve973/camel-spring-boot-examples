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

package org.apache.camel.example.springboot.numbers.common.service;

import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.camel.example.springboot.numbers.common.model.MessageTypes.STATS_COMMAND;

public abstract class ProcessNumbersRoutingParticipant extends RoutingParticipant {

    protected final String numberName;

    /**
     * Counter for the number of messages that this participant has processed.
     */
    protected final AtomicInteger processedCount = new AtomicInteger(0);

    /**
     * The count that was last reported via "stats" command message.
     */
    protected final AtomicInteger reportedCount = new AtomicInteger(0);

    public ProcessNumbersRoutingParticipant(
            String numberName,
            String subscriberId,
            String subscribeUri,
            String routingChannel,
            int subscriptionPriority,
            String predicate,
            String expressionLanguage,
            String consumeUri,
            String commandUri,
            ProducerTemplate producerTemplate) {
        super(subscriberId, subscribeUri, routingChannel, subscriptionPriority, predicate, expressionLanguage,
                consumeUri, commandUri, producerTemplate);
        this.numberName = numberName;
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.
     *
     * @param body the serialized command message
     */
    @Override
    @Consume(property = "consumeUri")
    public void consumeMessage(final byte[] body, @Header(value = "number") String number) {
        processedCount.incrementAndGet();
    }

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.SECONDS)
    public void sendStats() {
        int pCount = processedCount.get();
        if (pCount != reportedCount.get()) {
            CommandMessage command = CommandMessage.newBuilder()
                    .setCommand(STATS_COMMAND)
                    .putParams(numberName, String.valueOf(pCount))
                    .build();
            producerTemplate.send(
                    commandUri, ExchangeBuilder.anExchange(producerTemplate.getCamelContext())
                            .withHeader("command", STATS_COMMAND)
                            .withBody(command.toByteArray())
                            .build());
            reportedCount.set(pCount);
        }
    }
}
