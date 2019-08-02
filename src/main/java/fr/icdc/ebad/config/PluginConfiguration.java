package fr.icdc.ebad.config;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;

@Configuration
public class PluginConfiguration {
    private final GenericWebApplicationContext context;

    public PluginConfiguration(GenericWebApplicationContext context) {
        this.context = context;
    }

    @Bean
    public SpringPluginManager pluginManager() {
        SpringPluginManager pluginManager = new SpringPluginManager();
        pluginManager.setApplicationContext(context);
        return pluginManager;
    }
}
