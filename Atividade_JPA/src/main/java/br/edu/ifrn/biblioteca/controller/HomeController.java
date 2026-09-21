package br.edu.ifrn.biblioteca.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/api/status")
    public Map<String, String> home() {
        return Map.of(
                "aplicacao", "biblioteca-api",
                "status", "online",
                "h2Console", "/h2-console");
    }
}