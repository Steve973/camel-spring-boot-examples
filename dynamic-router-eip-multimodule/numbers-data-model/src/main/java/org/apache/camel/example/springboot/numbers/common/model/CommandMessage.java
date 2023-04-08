package org.apache.camel.example.springboot.numbers.common.model;

import java.util.Map;

public interface CommandMessage {

    String getCommand();

    Map<String, String> getParams();
}
