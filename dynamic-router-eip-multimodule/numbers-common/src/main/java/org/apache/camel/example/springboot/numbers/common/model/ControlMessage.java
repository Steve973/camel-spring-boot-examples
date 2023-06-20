package org.apache.camel.example.springboot.numbers.common.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ControlMessage {

    private final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    private String action;

    private String subscriberId;

    private String subscribeUri;

    private String routingChannel;

    private int subscriptionPriority;

    private String predicate;

    private String expressionLanguage;

    private String consumeUri;

    private String commandUri;

    public ControlMessage() {

    }

    public ControlMessage(String action, String subscriberId, String subscribeUri, String routingChannel,
                          int subscriptionPriority, String predicate, String expressionLanguage,
                          String consumeUri, String commandUri) {
        this.action = action;
        this.subscriberId = subscriberId;
        this.subscribeUri = subscribeUri;
        this.routingChannel = routingChannel;
        this.subscriptionPriority = subscriptionPriority;
        this.predicate = predicate;
        this.expressionLanguage = expressionLanguage;
        this.consumeUri = consumeUri;
        this.commandUri = commandUri;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getSubscribeUri() {
        return subscribeUri;
    }

    public void setSubscribeUri(String subscribeUri) {
        this.subscribeUri = subscribeUri;
    }

    public String getRoutingChannel() {
        return routingChannel;
    }

    public void setRoutingChannel(String routingChannel) {
        this.routingChannel = routingChannel;
    }

    public int getSubscriptionPriority() {
        return subscriptionPriority;
    }

    public void setSubscriptionPriority(int subscriptionPriority) {
        this.subscriptionPriority = subscriptionPriority;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getExpressionLanguage() {
        return expressionLanguage;
    }

    public void setExpressionLanguage(String expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public String getConsumeUri() {
        return consumeUri;
    }

    public void setConsumeUri(String consumeUri) {
        this.consumeUri = consumeUri;
    }

    public String getCommandUri() {
        return commandUri;
    }

    public void setCommandUri(String commandUri) {
        this.commandUri = commandUri;
    }

    @Override
    public String toString() {
        String result = "{\"error\": \"Could not convert control message to string\"}";
        try {
            result = objectMapper.writeValueAsString(this);
        } catch (Exception ignored) {
            // just return the prepared error message
        }
        return result;
    }
}
