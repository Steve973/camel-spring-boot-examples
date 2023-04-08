package org.apache.camel.example.springboot.numbers.mainrouter.service;

import org.apache.camel.Consume;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.example.springboot.numbers.common.model.CommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.GenerateNumbersCommandMessage;
import org.apache.camel.example.springboot.numbers.common.model.StatsCommand;
import org.apache.camel.example.springboot.numbers.common.service.RoutingParticipant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

@Service
public class MainRouterCommandService extends RoutingParticipant<StatsCommand> {

    private static final String STATS_COMMAND_ID = "stats";

    private final Map<String, Long> countsMap;

    public MainRouterCommandService(
            @Value("${number-generator.subscribe-uri}") String subscribeUri,
            @Value("${number-generator.routing-channel}") String routingChannel,
            @Value("${number-generator.subscription-priority}") int subscriptionPriority,
            @Value("${number-generator.consume-uri}") String consumeUri,
            @Value("${number-generator.command-uri}") String commandUri,
            ProducerTemplate producerTemplate) {
        super("stats", subscribeUri, routingChannel, subscriptionPriority,
                e -> e.getIn(CommandMessage.class).getCommand().equals(STATS_COMMAND_ID),
                consumeUri, commandUri, producerTemplate);
        this.countsMap = new TreeMap<>();
    }

    public Map<String, Long> getCountsMap() {
        return countsMap;
    }

    /**
     * Send the generate numbers message with the boolean toggle value.
     */
    public String sendGenerateNumbersCommand(Map<String, String> params) {
        producerTemplate.sendBody(commandUri, new GenerateNumbersCommandMessage(params));
        return "{\"status\": \"generate numbers command sent\"}";
    }

    /**
     * This method consumes messages that have matched the participant's rules
     * and have been routed to the participant.  It adds the results to the
     * results service.
     *
     * @param message the command message
     */
    @Consume(property = "consumeUri")
    public void consumeMessage(final StatsCommand message) {
        message.getParams().forEach((n, v) -> countsMap.put(n, Long.valueOf(v)));
    }
}
