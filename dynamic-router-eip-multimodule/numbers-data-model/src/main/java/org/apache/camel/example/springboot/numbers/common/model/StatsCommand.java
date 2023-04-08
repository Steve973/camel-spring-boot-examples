package org.apache.camel.example.springboot.numbers.common.model;

import java.util.Map;

public class StatsCommand implements CommandMessage {

    private static final String command = "stats";

    private final Map<String, String> params;

    public StatsCommand(Map<String, String> params) {
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
