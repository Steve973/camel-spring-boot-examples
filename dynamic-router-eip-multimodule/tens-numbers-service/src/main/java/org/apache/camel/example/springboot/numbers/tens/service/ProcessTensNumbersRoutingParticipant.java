package org.apache.camel.example.springboot.numbers.tens.service;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand;
import org.apache.camel.example.springboot.numbers.common.service.ProcessNumbersRoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand.PROCESS_NUMBER_COMMAND;

@Service
public class ProcessTensNumbersRoutingParticipant extends ProcessNumbersRoutingParticipant {

    public ProcessTensNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("tens", "processTensNumbers", subscribeUri, routingChannel, subscriptionPriority,
                e -> {
                    CommandMessage m = e.getIn(CommandMessage.class);
                    return m.getCommand().equals(PROCESS_NUMBER_COMMAND) && ((ProcessNumberCommand) m).getNumber() % 10 == 0;
                },
                consumeUri, commandUri, producerTemplate);
    }
}
