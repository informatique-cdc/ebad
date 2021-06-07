package fr.icdc.ebad.service;

import com.jcraft.jsch.*;
import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.util.RetourBatch;
import org.apache.commons.io.IOUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.util.io.pem.PemReader;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShellServiceTest {
    @InjectMocks
    private ShellService shellService;
    @Mock
    private JSch jsch;
    @Mock
    private Session session;
    @Mock
    private ChannelExec channelExec;
    @Mock
    private ChannelSftp channelSftp;
    @Spy
    private EbadProperties ebadProperties;

    @Test
    public void test() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        //convert privatekey : openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
        byte[] keyBytes = Files.readAllBytes(Paths.get("private_key.der"));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        //convert publicKey : openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der
        byte[] keyBytes2 = Files.readAllBytes(Paths.get("public_key.der"));

        X509EncodedKeySpec spec2 = new X509EncodedKeySpec(keyBytes2);
        PublicKey publicKey = kf.generatePublic(spec2);

        KeyPair keyPair = new KeyPair(publicKey, privateKey);

        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        long defaultTimeoutSeconds = 10;
        try (ClientSession session = client.connect("test", "test.fr", 22)
                .verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {
//            session.addPasswordIdentity(password);
            session.addPublicKeyIdentity(keyPair);
            session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createChannel(org.apache.sshd.common.channel.Channel.CHANNEL_SHELL)) {
                channel.setOut(responseStream);
                try {
                    channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                    String command = "echo \"hello\"\nexit\n";
                    try (OutputStream pipedIn = channel.getInvertedIn()) {
                        pipedIn.write(command.getBytes());
                        pipedIn.flush();
                    }

                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                            TimeUnit.SECONDS.toMillis(30));
                    String responseString = new String(responseStream.toByteArray());
                    System.out.println(responseString);
                } finally {
                    channel.close(false);
                }
            }
        } finally {
            client.stop();
        }
    }

    @Test
    public void test2() throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        String password = "xxx";

        FileReader keyReader = new FileReader("/xxx/id_rsa");
        PemReader pemReader = new PemReader(keyReader);
        PEMParser pemParser = new PEMParser(pemReader);
        Object pemKeyPair = pemParser.readObject();

        KeyPair keyPair;
        if (pemKeyPair instanceof PEMEncryptedKeyPair) {
            if (password == null) {
                System.err.println("Unable to import private key. Key is encrypted, but no password was provided.");
            }
            PEMDecryptorProvider decryptor = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
            PEMKeyPair decryptedKeyPair = ((PEMEncryptedKeyPair) pemKeyPair).decryptKeyPair(decryptor);
            keyPair = new JcaPEMKeyConverter().getKeyPair(decryptedKeyPair);
        } else {
            keyPair = new JcaPEMKeyConverter().getKeyPair((PEMKeyPair)pemKeyPair);
        }

        System.out.println(DateTime.now());
        SshClient client = SshClient.setUpDefaultClient();
        client.start();
        long defaultTimeoutSeconds = 10;
        try (ClientSession session = client.connect("xxx", "xxx", 22).verify()
                .getSession()) {
//            session.addPasswordIdentity(password);
            session.addPublicKeyIdentity(keyPair);
            session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createChannel(org.apache.sshd.common.channel.Channel.CHANNEL_SHELL)) {
                channel.setOut(responseStream);
                try {
                    channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                    String command = "sleep 120\nexit\n";
                    try (OutputStream pipedIn = channel.getInvertedIn()) {
                        pipedIn.write(command.getBytes());
                        pipedIn.flush();
                    }

                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                            TimeUnit.HOURS.toMillis(30));
                    String responseString = responseStream.toString();
                    System.out.println(responseString);
                } finally {
                    channel.close(false);
                }
            }
        } finally {
            client.stop();
            System.out.println(DateTime.now());
        }
    }

    @Before
    public void setup() {
        EbadProperties.SshProperties sshProperties = ebadProperties.getSsh();
        sshProperties.setPrivateKeyPath("/key");
        sshProperties.setPrivateKeyPassphrase("test");
        sshProperties.setLogin("ebad");
    }

    @Test
    public void runCommand() throws Exception {
        when(jsch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);

        InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode("test").array());
        when(channelExec.getInputStream()).thenReturn(is);
        when(session.openChannel(eq("exec"))).thenReturn(channelExec);
        doNothing().when(session).connect();
        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).build();
        RetourBatch retourBatch = shellService.runCommand(environnement, "echo 'hello'");

        verify(channelExec).setCommand(eq("/bin/bash echo 'hello'"));
        assertEquals(0, retourBatch.getReturnCode());
        assertEquals("test", retourBatch.getLogOut());
    }

    @Test
    public void getListFiles() throws Exception {
        when(jsch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);

        Vector<ChannelSftp.LsEntry> lsEntries = createEntries();
        when(channelSftp.ls(eq("/home/dir/subDir4"))).thenReturn(lsEntries);

        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);
        List<ChannelSftp.LsEntry> results = shellService.getListFiles(directory, "subDir4");

        assertEquals(lsEntries, results);
    }

    @Test
    public void removeFile() throws Exception {
        when(jsch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);


        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);
        shellService.removeFile(directory, "test.txt", null);

        verify(channelSftp).rm(eq("/home/dir/test.txt"));

    }

    @Test
    public void getFile() throws Exception {
        when(jsch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);

        Vector<ChannelSftp.LsEntry> lsEntries = createEntries();

        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);

        InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode("test").array());
        when(channelSftp.get(eq("/home/dir/test.txt"), any(SftpProgressMonitor.class))).thenReturn(is);

        InputStream result = shellService.getFile(directory, "test.txt", null);

        verify(channelSftp).get(eq("/home/dir/test.txt"), any(SftpProgressMonitor.class));
        String resultStr = IOUtils.toString(result, StandardCharsets.UTF_8.name());
        assertEquals("test", resultStr);
    }

    @Test
    public void uploadFile() throws Exception {
        when(jsch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);

        Vector<ChannelSftp.LsEntry> lsEntries = createEntries();

        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);

        InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode("test").array());

        shellService.uploadFile(directory, is, "test.txt", null);

        verify(channelSftp).put(eq(is), eq("/home/dir/test.txt"));
    }

    private Vector<ChannelSftp.LsEntry> createEntries() {

        Vector<ChannelSftp.LsEntry> vector = new Vector<>();

        vector.add(createSingleEntry("File 1", 123L, 1394525265, true));
        vector.add(createSingleEntry("File 2", 456L, 1394652161, false));
        vector.add(createSingleEntry("File 3", 789L, 1391879364, true));

        return vector;
    }

    private ChannelSftp.LsEntry createSingleEntry(String fileName, long size, int mTime, boolean directory) {
        SftpATTRS attributes = mock(SftpATTRS.class);
        ChannelSftp.LsEntry entry = mock(ChannelSftp.LsEntry.class);
        return entry;
    }
}
