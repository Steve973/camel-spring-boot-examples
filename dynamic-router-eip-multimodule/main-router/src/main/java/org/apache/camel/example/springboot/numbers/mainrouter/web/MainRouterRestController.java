package org.apache.camel.example.springboot.numbers.mainrouter.web;

import org.apache.camel.example.springboot.numbers.mainrouter.service.MainRouterCommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MainRouterRestController {

    final MainRouterCommandService statsService;

    public MainRouterRestController(MainRouterCommandService statsService) {
        this.statsService = statsService;
    }

    @GetMapping(path = "/counts")
    public Map<String, Long> getCounts() {
        return statsService.getCountsMap();
    }

    @PutMapping(path = "/generate")
    public String generate(@RequestParam Map<String, String> params) {
        return statsService.sendGenerateNumbersCommand(params);
    }
}
