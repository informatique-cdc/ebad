package fr.icdc.ebad.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.config.properties.EbadProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class JschConfiguration {
    private final EbadProperties ebadProperties;

    public JschConfiguration(EbadProperties ebadProperties) {
        this.ebadProperties = ebadProperties;
    }

    @Bean
    public JSch jsch() throws JSchException {
        JSch jsch = new JSch();
        if(null != ebadProperties.getSsh().getPrivateKeyPath() && Files.exists(Paths.get(ebadProperties.getSsh().getPrivateKeyPath()))) {
            jsch.addIdentity(ebadProperties.getSsh().getPrivateKeyPath(), ebadProperties.getSsh().getPrivateKeyPassphrase());
        }
        return jsch;
    }
}
