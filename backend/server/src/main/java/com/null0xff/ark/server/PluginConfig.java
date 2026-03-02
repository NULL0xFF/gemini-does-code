package com.null0xff.ark.server;

import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginRepository;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class PluginConfig {

    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager(Path.of("plugins")) {
            @Override
            protected PluginRepository createPluginRepository() {
                return new CompoundPluginRepository()
                        .add(new DevelopmentPluginRepository(getPluginsRoot()), this::isDevelopment)
                        .add(new JarPluginRepository(getPluginsRoot()), this::isNotDevelopment)
                        .add(new DefaultPluginRepository(getPluginsRoot()), this::isNotDevelopment);
            }
        };
    }

    @Bean
    public ApplicationRunner pluginLoader(SpringPluginManager pluginManager) {
        return args -> {
            pluginManager.loadPlugins();
            pluginManager.startPlugins();
        };
    }
}
