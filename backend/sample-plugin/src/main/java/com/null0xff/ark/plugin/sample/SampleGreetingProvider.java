package com.null0xff.ark.plugin.sample;

import com.null0xff.ark.api.GreetingProvider;
import org.pf4j.Extension;

@Extension
public class SampleGreetingProvider implements GreetingProvider {
    @Override
    public String getGreeting() {
        return "Hello from Sample Plugin!";
    }
}
