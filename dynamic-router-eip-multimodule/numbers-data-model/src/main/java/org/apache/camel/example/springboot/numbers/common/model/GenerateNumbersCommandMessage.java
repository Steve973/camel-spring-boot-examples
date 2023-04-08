package org.apache.camel.example.springboot.numbers.common.model;

import java.util.Map;

public final class GenerateNumbersCommandMessage implements CommandMessage {

    private static final String command = "generateNumbers";

    private final Map<String, String> params;

    public GenerateNumbersCommandMessage(
            Map<String, String> params) {
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
