package org.apache.camel.example.springboot.numbers.gen.service;

import jakarta.annotation.PostConstruct;
import org.apache.camel.Consume;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dynamicrouter.DynamicRouterControlMessage;
import org.apache.camel.example.springboot.numbers.gen.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.gen.model.GenerateNumbersCommandMessage;
import org.apache.camel.example.springboot.numbers.gen.model.ProcessNumberCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GenerateNumbersRoutingParticipant {

    private static final String GENERATE_NUMBERS_COMMAND = "generateNumbers";

    private static final String PROCESS_NUMBER_COMMAND = "processNumber";

    private static final String PARAM_TYPE = "type";

    private static final String PARAM_TYPE_SEQUENTIAL = "sequential";

    private static final String PARAM_TYPE_RANDOM = "random";

    private static final String PARAM_TYPE_STOP = "stop";

    private static final String PARAM_TYPE_START = "start";

    private static final String PARAM_TYPE_LIMIT = "limit";

    /**
     * The dynamic router control channel URI where subscribe messages will
     * be sent.
     */
    protected final String subscribeUri;

    /**
     * The channel of the dynamic router to send messages.
     */
    protected final String routingChannel;

    /**
     * The priority of the processor when evaluated by the dynamic router.  Lower
     * number means higher priority.
     */
    protected final int priority;

    /**
     * The {@link Predicate} by which exchanges are evaluated for suitability for
     * a routing participant.
     */
    protected final Predicate predicate;

    /**
     * The URI that a participant implementation will listen on for messages
     * that match its rules.
     */
    protected final String consumeUri;

    /**
     * URI to send a command to (for dynamic routing).
     */
    protected final String commandUri;

    /**
     * The {@link ProducerTemplate} to send subscriber messages to the dynamic
     * router control channel.
     */
    protected final ProducerTemplate producerTemplate;

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
        this.subscribeUri = subscribeUri;
        this.routingChannel = routingChannel;
        this.priority = subscriptionPriority;
        this.predicate = e -> e.getIn(CommandMessage.class).command().equals(GENERATE_NUMBERS_COMMAND);
        this.consumeUri = consumeUri;
        this.commandUri = commandUri;
        this.producerTemplate = producerTemplate;
    }

    /**
     * Send the subscribe message after this service instance is created.
     */
    @PostConstruct
    private void subscribe() {
        producerTemplate.sendBody(subscribeUri, createSubscribeMessage());
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param message the command message
     */
    @Consume(property = "consumeUri")
    public void consumeMessage(final GenerateNumbersCommandMessage message) {
        Map<String, String> params = message.params();
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
     * Create a {@link DynamicRouterControlMessage} based on parameters from the
     * implementing class.
     *
     * @return the {@link DynamicRouterControlMessage}
     */
    protected DynamicRouterControlMessage createSubscribeMessage() {
        return new DynamicRouterControlMessage.SubscribeMessageBuilder()
                .id(GENERATE_NUMBERS_COMMAND)
                .channel(this.routingChannel)
                .priority(this.priority)
                .endpointUri(this.subscribeUri)
                .predicate(this.predicate)
                .build();
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
            producerTemplate.asyncSendBody(commandUri, new ProcessNumberCommand(PROCESS_NUMBER_COMMAND, number, Map.of()));
        }
    }
}
