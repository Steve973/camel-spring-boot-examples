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

package org.apache.camel.example.springboot.numbers.fives.service;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.service.ProcessNumbersRoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProcessFivesNumbersRoutingParticipant extends ProcessNumbersRoutingParticipant {

    public ProcessFivesNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.predicate}") String predicate,
            @Value("${number-generator.expression-language}") String expressionLanguage,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("fives", "processFivesNumbers", subscribeUri, routingChannel, subscriptionPriority,
                predicate, expressionLanguage, consumeUri, commandUri, producerTemplate);
    }
}
