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

package org.apache.camel.example.springboot.numbers.sixes.service;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand;
import org.apache.camel.example.springboot.numbers.common.service.ProcessNumbersRoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand.PROCESS_NUMBER_COMMAND;

@Service
public class ProcessSixesNumbersRoutingParticipant extends ProcessNumbersRoutingParticipant {

    public ProcessSixesNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("sixes", "processSixesNumbers", subscribeUri, routingChannel, subscriptionPriority,
                e -> {
                    CommandMessage m = e.getIn(CommandMessage.class);
                    return m.getCommand().equals(PROCESS_NUMBER_COMMAND) && ((ProcessNumberCommand) m).getNumber() % 6 == 0;
                },
                consumeUri, commandUri, producerTemplate);
    }
}
