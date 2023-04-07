package org.apache.camel.example.springboot.numbers.mainrouter.model;

import java.util.Map;

public interface CommandMessage {

    String command();

    Map<String, String> params();
}
