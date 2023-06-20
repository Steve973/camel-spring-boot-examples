package org.apache.camel.example.springboot.numbers.common.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CommandMessage {

    private final ObjectMapper objectMapper = new ObjectMapper(new JsonFactory());

    private String command;

    private Map<String, String> params;

    public CommandMessage() {

    }

    public CommandMessage(String command, Map<String, String> params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        String result = "{\"error\": \"Could not convert command message to string\"}";
        try {
            result = objectMapper.writeValueAsString(this);
        } catch (Exception ignored) {
            // just return the prepared error message
        }
        return result;
    }
}
