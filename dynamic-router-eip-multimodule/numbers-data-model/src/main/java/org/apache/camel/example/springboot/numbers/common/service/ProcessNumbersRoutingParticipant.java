/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.camel.example.springboot.numbers.common.service;

import org.apache.camel.Consume;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand;
import org.apache.camel.example.springboot.numbers.common.model.StatsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ProcessNumbersRoutingParticipant extends RoutingParticipant<ProcessNumberCommand> {

    protected static final Logger LOG = LoggerFactory.getLogger(ProcessNumbersRoutingParticipant.class);

    private final String numberName;

    /**
     * Counter for the number of messages that this participant has processed.
     */
    private final AtomicInteger processedCount = new AtomicInteger(0);

    /**
     * The count that was last reported via "stats" command message.
     */
    private final AtomicInteger reportedCount = new AtomicInteger(0);

    public ProcessNumbersRoutingParticipant(
            String numberName,
            String subscriberId,
            String subscribeUri,
            String routingChannel,
            int subscriptionPriority,
            Predicate predicate,
            String consumeUri,
            String commandUri,
            ProducerTemplate producerTemplate) {
        super(subscriberId, subscribeUri, routingChannel, subscriptionPriority, predicate, consumeUri, commandUri, producerTemplate);
        this.numberName = numberName;
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param message the command message
     */
    @Consume(property = "consumeUri")
    public void consumeMessage(final ProcessNumberCommand message) {
        int number = message.getNumber();
        int count = processedCount.incrementAndGet();
        LOG.info("Processed {} number: {}, total processed: {}", numberName, number, count);
    }

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.SECONDS)
    private void sendStats() {
        int pCount = processedCount.get();
        int rCount = reportedCount.get();
        if (pCount != rCount) {
            producerTemplate.sendBody(commandUri, new StatsCommand(Map.of(numberName, String.valueOf(pCount))));
            reportedCount.set(rCount);
        }
    }
}
