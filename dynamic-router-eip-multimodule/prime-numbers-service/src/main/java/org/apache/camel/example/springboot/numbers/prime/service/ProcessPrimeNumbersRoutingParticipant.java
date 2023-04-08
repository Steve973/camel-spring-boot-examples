package org.apache.camel.example.springboot.numbers.prime.service;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand;
import org.apache.camel.example.springboot.numbers.common.service.ProcessNumbersRoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand.PROCESS_NUMBER_COMMAND;

@Service
public class ProcessPrimeNumbersRoutingParticipant extends ProcessNumbersRoutingParticipant {

    public ProcessPrimeNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("prime", "processPrimeNumbers", subscribeUri, routingChannel, subscriptionPriority,
                e -> {
                    CommandMessage m = e.getIn(CommandMessage.class);
                    if (m.getCommand().equals(PROCESS_NUMBER_COMMAND)) {
                        int n = ((ProcessNumberCommand) m).getNumber();
                        // 2 is the first prime number
                        if (n <= 2) {
                            return n == 2;
                        }
                        // no other even numbers are prime
                        if (n % 2 == 0) {
                            return false;
                        }
                        // only some odd numbers might be prime
                        int max = (int) Math.sqrt(n) + 1;
                        for (int i = 3; i < max; i += 2) {
                            if (n % i == 0)
                                return false;
                        }
                        return true;
                    } else {
                        return false;
                    }
                },
                consumeUri, commandUri, producerTemplate);
    }
}
