package com.null0xff.ark.server;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.null0xff.ark.api.GreetingProvider;

import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    private final SpringPluginManager pluginManager;

    public HelloController(SpringPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/api/hello")
    public Map<String, String> hello() {
        List<GreetingProvider> greetings = pluginManager.getExtensions(GreetingProvider.class);
        String message = "Hello from Ark Resolver Server!";
        if (!greetings.isEmpty()) {
            message += " Plugins say: " + greetings.get(0).getGreeting();
        }
        return Map.of("message", message);
    }
}
