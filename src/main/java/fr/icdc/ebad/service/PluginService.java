package fr.icdc.ebad.service;

import fr.icdc.ebad.plugin.plugin.EnvironnementConnectorPlugin;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginService.class);
    private final SpringPluginManager springPluginManager;
    private final List<EnvironnementConnectorPlugin> environnementConnectorPlugins;

    public PluginService(SpringPluginManager springPluginManager, List<EnvironnementConnectorPlugin> environnementConnectorPlugins) {
        this.springPluginManager = springPluginManager;
        this.environnementConnectorPlugins = environnementConnectorPlugins;
    }

    @PostConstruct
    public void listAllPlugins() {
        LOGGER.info("THE FOLLOWING PLUGINS ARE LOADED");
        for (PluginWrapper plugin : springPluginManager.getPlugins()) {
            LOGGER.info("Plugin {} {}", plugin.getPluginId(), plugin.getPluginState());
        }
    }
}
