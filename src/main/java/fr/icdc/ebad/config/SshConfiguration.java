package fr.icdc.ebad.config;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.Security;

@Configuration
public class SshConfiguration {

    @Bean
    KeyPair keyPairEbad(EbadProperties ebadProperties) throws EbadServiceException {
        Security.addProvider(new BouncyCastleProvider());
        String password = ebadProperties.getSsh().getPrivateKeyPassphrase();

        FileReader keyReader;
        try {
            keyReader = new FileReader(ebadProperties.getSsh().getPrivateKeyPath());
            PemReader pemReader = new PemReader(keyReader);
            PEMParser pemParser = new PEMParser(pemReader);
            Object pemKeyPair = pemParser.readObject();

            if (pemKeyPair instanceof PEMEncryptedKeyPair) {
                if (password == null) {
                    throw new EbadServiceException("Unable to import private key. Key is encrypted, but no password was provided.");
                }
                PEMDecryptorProvider decryptor = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
                PEMKeyPair decryptedKeyPair = ((PEMEncryptedKeyPair) pemKeyPair).decryptKeyPair(decryptor);
                return new JcaPEMKeyConverter().getKeyPair(decryptedKeyPair);
            } else {
                return new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) pemKeyPair);
            }
        }catch (IOException e){
            throw new EbadServiceException("Error when trying to read ssh key file", e);
        }
    }
}
