package com.null0xff.ark.plugin.sample;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class SamplePlugin extends Plugin {
    public SamplePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
    
    @Override
    public void start() {
        System.out.println("SamplePlugin.start()");
    }
    
    @Override
    public void stop() {
        System.out.println("SamplePlugin.stop()");
    }
}
