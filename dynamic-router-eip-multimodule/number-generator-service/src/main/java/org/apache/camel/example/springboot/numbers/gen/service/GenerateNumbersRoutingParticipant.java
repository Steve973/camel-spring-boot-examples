package org.apache.camel.example.springboot.numbers.gen.service;

import org.apache.camel.Consume;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.GenerateNumbersCommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.ProcessNumberCommand;
import org.apache.camel.example.springboot.numbers.common.service.RoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GenerateNumbersRoutingParticipant extends RoutingParticipant<GenerateNumbersCommandMessage> {

    private static final String GENERATE_NUMBERS_COMMAND = "generateNumbers";

    private static final String PARAM_TYPE = "type";

    private static final String PARAM_TYPE_SEQUENTIAL = "sequential";

    private static final String PARAM_TYPE_RANDOM = "random";

    private static final String PARAM_TYPE_STOP = "stop";

    private static final String PARAM_TYPE_START = "start";

    private static final String PARAM_TYPE_LIMIT = "limit";

    /**
     * Flag so that subsequent commands can be used to halt number messages.
     */
    private final AtomicBoolean runFlag = new AtomicBoolean(false);

    /**
     * Instance for generating random numbers.
     */
    private final Random random = new Random();

    public GenerateNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.generate-numbers-consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super(GENERATE_NUMBERS_COMMAND, subscribeUri, routingChannel, subscriptionPriority,
                e -> e.getIn(CommandMessage.class).getCommand().equals(GENERATE_NUMBERS_COMMAND),
                consumeUri, commandUri, producerTemplate);
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param message the command message
     */
    @Override
    @Consume(property = "consumeUri")
    public void consumeMessage(final GenerateNumbersCommandMessage message) {
        Map<String, String> params = message.getParams();
        String type = params.getOrDefault(PARAM_TYPE, PARAM_TYPE_SEQUENTIAL);
        int start = Integer.parseInt(params.getOrDefault(PARAM_TYPE_START, "0"));
        int limit = Integer.parseInt(params.getOrDefault(PARAM_TYPE_LIMIT, "0"));
        switch (type) {
            case PARAM_TYPE_SEQUENTIAL, PARAM_TYPE_RANDOM -> {
                this.runFlag.set(true);
                generateNumbers(type, start, limit);
            }
            case PARAM_TYPE_STOP -> this.runFlag.set(false);
        }
    }

    /**
     * When a command has been received to generate numbers, this will continuously generate
     * numbers and send them in a command to have recipients process the numbers.  It will
     * only stop when a limit (if any) is reached, or if a subsequent command instructs
     * number message generation to stop
     *
     * @param type type of numbers to create (sequential or random)
     * @param start the number to start with (when in sequential mode)
     * @param limit the count of numbers to produce (zero means Integer.MAX_VALUE)
     */
     protected void generateNumbers(String type, int start, int limit) {
        int current = start;
        int remaining = limit == 0 ? Integer.MAX_VALUE : limit;
        while (this.runFlag.get() && remaining > 0) {
            remaining--;
            int number = PARAM_TYPE_SEQUENTIAL.equals(type) ? current++ : random.nextInt(0, Integer.MAX_VALUE);
            producerTemplate.asyncSendBody(commandUri, new ProcessNumberCommand(number, Map.of()));
        }
    }
}
