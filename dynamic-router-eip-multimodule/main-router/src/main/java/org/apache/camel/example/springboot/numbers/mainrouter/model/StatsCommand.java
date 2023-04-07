package org.apache.camel.example.springboot.numbers.mainrouter.model;

import java.util.Map;

public record StatsCommand(
        String command,
        Map<String, String> params) implements CommandMessage {
}
