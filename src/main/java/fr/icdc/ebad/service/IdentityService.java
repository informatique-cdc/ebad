package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.repository.IdentityRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.KeyPair;
import java.security.Security;
import java.util.Optional;

@Service
public class IdentityService {
    private final IdentityRepository identityRepository;

    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Transactional
    public Identity saveIdentity(Identity identity) {
        return identityRepository.save(identity);
    }

    @Transactional(readOnly = true)
    public Optional<Identity> getIdentity(Long id) {
        return identityRepository.findById(id);
    }

    @Transactional
    public void deleteIdentity(Long id) {
        identityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Identity> findWithoutApp(Pageable pageable) {
        return identityRepository.findAllByAvailableApplicationNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Identity> findAllByApplication(Long applicationId, Pageable pageable) {
        return identityRepository.findAllByAvailableApplicationId(applicationId, pageable);
    }


    public KeyPair createKeyPair(Identity identity) throws EbadServiceException {
        Security.addProvider(new BouncyCastleProvider());
        String password = identity.getPassphrase();

        try(
                PemReader pemReader = new PemReader(openReader(identity));
                PEMParser pemParser = new PEMParser(pemReader)
        ) {
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
            throw new EbadServiceException("Error when trying to read ssh key file\n" +
                    "Make sur use RSA keys, try to convert your key with \n" +
                    "ssh-keygen -p -P \"old passphrase\" -N \"new passphrase\" -m pem -f path/to/key \n" +
                    "THIS OVERWRITE YOUR KEY SO MAKE A BACKUP BEFORE", e);
        }
    }

    private Reader openReader(Identity identity) throws FileNotFoundException, EbadServiceException {
        if(identity.getPrivatekeyPath() != null)
            return new FileReader(identity.getPrivatekeyPath());
        if(identity.getPrivatekey() != null)
            return new StringReader(identity.getPrivatekey());

        throw new EbadServiceException("No key is provided");
    }
}
