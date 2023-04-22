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

import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.apache.camel.example.springboot.numbers.common.model.MessageTypes.GENERATE_NUMBERS_COMMAND;

@Service
public class NumberGenerationCommandService {

    private final String commandUri;

    private final ProducerTemplate producerTemplate;

    public NumberGenerationCommandService(
            @Value("${main-router.dynamic-router-component.command-entrypoint}") String commandUri,
            ProducerTemplate producerTemplate) {
        this.commandUri = commandUri;
        this.producerTemplate = producerTemplate;
    }

    /**
     * Send the generate numbers message with the boolean toggle value.
     */
    public String sendGenerateNumbersCommand(String limit) {
        CommandMessage generateNumbersCommandMessage = CommandMessage.newBuilder()
                .setCommand(GENERATE_NUMBERS_COMMAND)
                .putParams("limit", limit)
                .build();
        Map<String, Object> headers = Map.of(
                KafkaConstants.KEY, "numbers",
                "command", GENERATE_NUMBERS_COMMAND);
        producerTemplate.sendBodyAndHeaders(commandUri, generateNumbersCommandMessage.toByteArray(), headers);
        return "{\"status\": \"generate numbers command sent\"}";
    }
}
