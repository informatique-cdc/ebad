package fr.icdc.ebad;

import fr.icdc.ebad.config.properties.EbadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@SpringBootApplication
@EnableConfigurationProperties(EbadProperties.class)
@EnableAsync
@EnableScheduling
public class Application {
    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(Application.class);

    private final Environment env;
    private static ConfigurableApplicationContext applicationContext;

    public Application(Environment env) {
        this.env = env;
    }

    /**
     * Main method, used to run the application.
     *
     * @param args all main args
     * @throws UnknownHostException exception
     */
    public static void main(String[] args) throws UnknownHostException {
        SpringApplication app = new SpringApplication(Application.class);

        applicationContext = app.run(args);
        Environment env = applicationContext.getEnvironment();
        APPLICATION_LOGGER.info("Access URLs:\n----------------------------------------------------------\n\t" +
                "Local: \t\thttp://127.0.0.1:{}\n\t" +
                "External: \thttp://{}:{}\n----------------------------------------------------------", env.getProperty("server.port"), InetAddress.getLocalHost().getHostAddress(), env.getProperty("server.port"));
    }

    public static void restart() {
        ApplicationArguments args = applicationContext.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            applicationContext.close();
            applicationContext = SpringApplication.run(Application.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }


    /**
     * Initializes application.
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     *
     */
    @PostConstruct
    public void initApplication() {
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");

            if (Modifier.isFinal(field.getModifiers())) {
                Field modifiers = Field.class.getDeclaredField("modifiers");
                modifiers.setAccessible(true);
                modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
            field.setAccessible(true);
            field.setBoolean(null, false);
            field.setAccessible(false);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
        	APPLICATION_LOGGER.error("Erreur when disabling JceSecurity", e);
        }
        APPLICATION_LOGGER.info("Version Application : {}", Application.class.getPackage().getImplementationVersion());
        if (env.getActiveProfiles().length == 0) {
        	APPLICATION_LOGGER.warn("No Spring profile configured, running with default configuration");
        } else {
            if (APPLICATION_LOGGER.isInfoEnabled()) {
                APPLICATION_LOGGER.info("Running with Spring profile(s) : {}", Arrays.toString(env.getActiveProfiles()));
            }
        }
    }
}
