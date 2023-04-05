package org.apache.camel.example.springboot.numbers.mainrouter.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param routingChannel    The dynamic router channel.
 * @param subscribeUri      The dynamic router control channel URI
 * @param messageEntrypoint The URI where messages will be sent to be dynamically routed.
 * @param recipientMode     The recipient mode -- first matching filter only, or all matching filters.
 */
@Validated
@ConfigurationProperties(prefix = "main-router")
public record MainRouterConfig(
        @NotBlank String routingChannel,
        @NotBlank String subscribeUri,
        @NotBlank String messageEntrypoint,
        @NotBlank @Pattern(regexp = "^firstMatch|allMatch$") String recipientMode) {
}
