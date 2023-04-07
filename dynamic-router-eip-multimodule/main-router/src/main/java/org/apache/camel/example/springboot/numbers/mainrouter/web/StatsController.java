package org.apache.camel.example.springboot.numbers.mainrouter.web;

import org.apache.camel.example.springboot.numbers.mainrouter.service.StatsCommandRoutingParticipant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StatsController {

    final StatsCommandRoutingParticipant statsService;

    public StatsController(StatsCommandRoutingParticipant statsService) {
        this.statsService = statsService;
    }

    @GetMapping(path = "/counts")
    public Map<String, Long> getCounts() {
        return statsService.getCountsMap();
    }
}
