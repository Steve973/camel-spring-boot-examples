package org.apache.camel.example.springboot.numbers.odd.model;

import java.util.Map;

public interface CommandMessage {

    String command();

    Map<String, String> params();
}
