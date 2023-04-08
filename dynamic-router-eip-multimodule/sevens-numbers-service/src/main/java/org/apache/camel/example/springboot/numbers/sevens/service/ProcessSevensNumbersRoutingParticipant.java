package org.apache.camel.example.springboot.numbers.sevens.service;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand;
import org.apache.camel.example.springboot.numbers.common.service.ProcessNumbersRoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand.PROCESS_NUMBER_COMMAND;

@Service
public class ProcessSevensNumbersRoutingParticipant extends ProcessNumbersRoutingParticipant {

    public ProcessSevensNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("sevens", "processSevensNumbers", subscribeUri, routingChannel, subscriptionPriority,
                e -> {
                    CommandMessage m = e.getIn(CommandMessage.class);
                    return m.getCommand().equals(PROCESS_NUMBER_COMMAND) && ((ProcessNumberCommand) m).getNumber() % 7 == 0;
                },
                consumeUri, commandUri, producerTemplate);
    }
}
