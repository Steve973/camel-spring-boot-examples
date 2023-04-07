package org.apache.camel.example.springboot.numbers.even.model;

import java.util.Map;

public record ProcessNumberCommand(
        String command,
        int number,
        Map<String, String> params) implements CommandMessage {
}
