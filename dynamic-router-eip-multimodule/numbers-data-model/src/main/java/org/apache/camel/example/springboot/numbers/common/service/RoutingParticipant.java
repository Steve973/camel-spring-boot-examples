/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.camel.example.springboot.numbers.common.service;

import jakarta.annotation.PostConstruct;
import org.apache.camel.Consume;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.dynamicrouter.DynamicRouterControlMessage;

public abstract class RoutingParticipant<T> {

    protected final String subscriberId;

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

    public RoutingParticipant(
            String subscriberId,
            String subscribeUri,
            String routingChannel,
            int subscriptionPriority,
            Predicate predicate,
            String consumeUri,
            String commandUri,
            ProducerTemplate producerTemplate) {
        this.subscriberId = subscriberId;
        this.subscribeUri = subscribeUri;
        this.routingChannel = routingChannel;
        this.priority = subscriptionPriority;
        this.predicate = predicate;
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
    public abstract void consumeMessage(final T message);

    /**
     * Create a {@link DynamicRouterControlMessage} based on parameters from the
     * implementing class.
     *
     * @return the {@link DynamicRouterControlMessage}
     */
    protected DynamicRouterControlMessage createSubscribeMessage() {
        return new DynamicRouterControlMessage.SubscribeMessageBuilder()
                .id(subscriberId)
                .channel(this.routingChannel)
                .priority(this.priority)
                .endpointUri(this.subscribeUri)
                .predicate(this.predicate)
                .build();
    }
}
