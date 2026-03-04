package com.null0xff.ark.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.null0xff.ark.api.GreetingProvider;

import java.util.List;
import java.util.Map;

/**
 * Controller for testing server status and plugin system functionality.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "System Status", description = "Endpoints for checking server health and plugin integration")
public class HelloController {

    private final SpringPluginManager pluginManager;

    /**
     * Basic hello endpoint that aggregates greetings from active PF4J plugins.
     *
     * @return A map containing a greeting message
     */
    @Operation(summary = "Hello World & Plugin Test", description = "Returns a greeting from the core server and appends messages from any active plugins.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved greeting message")
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
