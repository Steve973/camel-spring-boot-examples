package org.apache.camel.example.springboot.numbers.all.service;

import jakarta.annotation.PostConstruct;
import org.apache.camel.Consume;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dynamicrouter.DynamicRouterControlMessage;
import org.apache.camel.example.springboot.numbers.all.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.all.model.ProcessNumberCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProcessAllNumbersRoutingParticipant {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessAllNumbersRoutingParticipant.class);

    private static final String PROCESS_ALL_NUMBERS_ID = "processAllNumbers";

    private static final String PROCESS_NUMBER_COMMAND = "processNumber";

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
     * Counter for the number of messages that this participant has processed.
     */
    private final AtomicInteger processedCount = new AtomicInteger(0);

    public ProcessAllNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        this.subscribeUri = subscribeUri;
        this.routingChannel = routingChannel;
        this.priority = subscriptionPriority;
        this.predicate = e -> e.getIn(CommandMessage.class).command().equals(PROCESS_NUMBER_COMMAND);
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
    public void consumeMessage(final ProcessNumberCommand message) {
        int number = message.number();
        int count = processedCount.incrementAndGet();
        LOG.info("Processed number: {}, total processed: {}", number, count);
    }

    /**
     * Create a {@link DynamicRouterControlMessage} based on parameters from the
     * implementing class.
     *
     * @return the {@link DynamicRouterControlMessage}
     */
    protected DynamicRouterControlMessage createSubscribeMessage() {
        return new DynamicRouterControlMessage.SubscribeMessageBuilder()
                .id(PROCESS_ALL_NUMBERS_ID)
                .channel(this.routingChannel)
                .priority(this.priority)
                .endpointUri(this.subscribeUri)
                .predicate(this.predicate)
                .build();
    }
}
