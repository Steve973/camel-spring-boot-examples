package org.apache.camel.example.springboot.numbers.prime.service;

import jakarta.annotation.PostConstruct;
import org.apache.camel.Consume;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dynamicrouter.DynamicRouterControlMessage;
import org.apache.camel.example.springboot.numbers.prime.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.prime.model.ProcessNumberCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProcessPrimeNumbersRoutingParticipant {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessPrimeNumbersRoutingParticipant.class);

    private static final String PROCESS_PRIME_NUMBER_ID = "processPrimeNumbers";

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

    public ProcessPrimeNumbersRoutingParticipant(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        this.subscribeUri = subscribeUri;
        this.routingChannel = routingChannel;
        this.priority = subscriptionPriority;
        this.predicate = e -> {
            CommandMessage m = e.getIn(CommandMessage.class);
            if (m.command().equals(PROCESS_NUMBER_COMMAND)) {
                int n = ((ProcessNumberCommand) m).number();
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
        };
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
        LOG.info("Processed prime number: {}, total processed: {}", number, count);
    }

    /**
     * Create a {@link DynamicRouterControlMessage} based on parameters from the
     * implementing class.
     *
     * @return the {@link DynamicRouterControlMessage}
     */
    protected DynamicRouterControlMessage createSubscribeMessage() {
        return new DynamicRouterControlMessage.SubscribeMessageBuilder()
                .id(PROCESS_PRIME_NUMBER_ID)
                .channel(this.routingChannel)
                .priority(this.priority)
                .endpointUri(this.subscribeUri)
                .predicate(this.predicate)
                .build();
    }
}
