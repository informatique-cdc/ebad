package fr.icdc.ebad.config;

import fr.icdc.ebad.config.properties.EbadProperties;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.nio.file.Paths;

@Configuration
public class PluginConfiguration {
    private final GenericWebApplicationContext context;
    private final EbadProperties ebadProperties;

    public PluginConfiguration(GenericWebApplicationContext context, EbadProperties ebadProperties) {
        this.context = context;
        this.ebadProperties = ebadProperties;
    }

    @Bean
    public SpringPluginManager springPluginManager() {
        SpringPluginManager pluginManager = new SpringPluginManager(Paths.get(ebadProperties.getPlugin().getPath()));
        pluginManager.setApplicationContext(context);
        return pluginManager;
    }
}
