package org.apache.camel.example.springboot.numbers.all.service;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.service.ProcessNumbersRoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand.PROCESS_NUMBER_COMMAND;

@Service
public class ProcessAllNumbersRoutingParticipant extends ProcessNumbersRoutingParticipant {

    public ProcessAllNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("all", "processAllNumbers", subscribeUri, routingChannel, subscriptionPriority,
                e -> e.getIn(CommandMessage.class).getCommand().equals(PROCESS_NUMBER_COMMAND),
                consumeUri, commandUri, producerTemplate);
    }
}
