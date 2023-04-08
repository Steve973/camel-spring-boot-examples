package org.apache.camel.example.springboot.numbers.common.model;

import java.util.Map;

public final class ProcessNumberCommand implements CommandMessage {

    public static final String PROCESS_NUMBER_COMMAND = "processNumber";

    private final int number;

    private final Map<String, String> params;

    public ProcessNumberCommand(
            int number,
            Map<String, String> params) {
        this.number = number;
        this.params = params;
    }

    public String getCommand() {
        return PROCESS_NUMBER_COMMAND;
    }

    public int getNumber() {
        return number;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
