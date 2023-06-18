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

package org.apache.camel.example.springboot.numbers.prime.service;

import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.service.ProcessNumbersRoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ProcessPrimeNumbersRoutingParticipant extends ProcessNumbersRoutingParticipant {

    public ProcessPrimeNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.predicate}") String predicate,
            @Value("${number-generator.expression-language}") String expressionLanguage,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("prime", "processPrimeNumbers", subscribeUri, routingChannel, subscriptionPriority,
                predicate, expressionLanguage, consumeUri, commandUri, producerTemplate);
    }

    /**
     * Evaluates if the given number is prime.  Before calling this function,
     * ensure that the applied number is odd and greater than 3.
     */
    public static final Function<Integer, Boolean> evaluatePrime = n -> {
        // only some odd numbers might be prime
        int max = (int) Math.sqrt(n) + 1;
        for (int i = 3; i < max; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    };

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param body the serialized command message
     */
    @Override
    @Consume(property = "consumeUri")
    public void consumeMessage(final byte[] body, @Header(value = "number") String number) {
        int n = Integer.parseInt(number == null ? "0" : number);
        if (n > 3 && n % 2 != 0 && evaluatePrime.apply(n)) {
            processedCount.incrementAndGet();
        } else if (n == 2 || n == 3) {
            processedCount.incrementAndGet();
        }
    }
}
