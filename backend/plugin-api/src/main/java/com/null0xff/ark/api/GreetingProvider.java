package com.null0xff.ark.api;

import org.pf4j.ExtensionPoint;

public interface GreetingProvider extends ExtensionPoint {
    String getGreeting();
}
