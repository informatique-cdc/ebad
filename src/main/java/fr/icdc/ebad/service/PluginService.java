package fr.icdc.ebad.service;

import fr.icdc.ebad.plugin.plugin.EnvironnementConnectorPlugin;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public void listAllPlugins() {
        LOGGER.error("listAllPlugins");
        for (PluginWrapper plugin : springPluginManager.getPlugins()) {
            try {
                if (!environnementConnectorPlugins.isEmpty())
                    environnementConnectorPlugins.get(0).discoverFromApp("test", new ArrayList<>());
            } catch (Exception e) {
                LOGGER.error("erreur");
            }
            LOGGER.error("Plugin {} {}", plugin.getPluginId(), plugin.getPluginState());
            plugin.getPluginManager().stopPlugin(plugin.getPluginId());
            springPluginManager.disablePlugin(plugin.getPluginId());
            //Application.restart();
            try {
                if (!environnementConnectorPlugins.isEmpty())
                    environnementConnectorPlugins.get(0).discoverFromApp("test", new ArrayList<>());
            } catch (Exception e) {
                LOGGER.error("erreur");
            }
        }
        System.out.println("XXXXX");
    }
}
