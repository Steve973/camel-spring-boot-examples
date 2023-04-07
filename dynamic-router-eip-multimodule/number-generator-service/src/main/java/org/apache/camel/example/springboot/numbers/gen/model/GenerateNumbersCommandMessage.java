package org.apache.camel.example.springboot.numbers.gen.model;

import java.util.Map;

public record GenerateNumbersCommandMessage(
        String command,
        Map<String, String> params) implements CommandMessage {
}
