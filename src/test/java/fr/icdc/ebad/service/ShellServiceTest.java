package fr.icdc.ebad.service;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.Terminal;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.TerminalRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.server.subsystem.SubsystemFactory;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShellServiceTest {
    private ShellService shellService;

    @Spy
    private EbadProperties ebadProperties;

    @Mock
    private IdentityService identityService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private TerminalRepository terminalRepository;

    @Mock
    private JobScheduler jobScheduler;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private static final String USERNAME = "ebad";
    private static final String PASSWORD = "password";

    private SshServer sshd;

    @Before
    public void setup() throws IOException {
        shellService = new ShellService(ebadProperties, identityService, messagingTemplate, jobScheduler, terminalRepository);
        EbadProperties.SshProperties sshProperties = ebadProperties.getSsh();
        sshProperties.setPort(2048);
        setupSSHServer();
    }

    @After
    public void tearDown() throws IOException {
        sshd.stop();
    }

    private void setupSSHServer() throws IOException {
        sshd = SshServer.setUpDefaultServer();
        final PublicKey allowedKey;
        sshd.setPasswordAuthenticator((username, password, session) ->
                StringUtils.equals(username, USERNAME) && StringUtils.equals(password, PASSWORD)
        );

        sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {

            @Override
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                return false;
                //return key.equals(allowedKey);
            }

        });

        sshd.setPort(2048);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Files.createTempFile("host_file", ".ser")));
        sshd.setSubsystemFactories(Collections.<SubsystemFactory>singletonList(new SftpSubsystemFactory()));
        sshd.setFileSystemFactory(new VirtualFileSystemFactory(Files.createTempDirectory("SFTP_TEMP")));
        sshd.setCommandFactory(new ProcessShellCommandFactory());
        sshd.start();
    }

    @Test
    public void runCommand() throws EbadServiceException {
        Norme norme = Norme.builder().commandLine("$1").build();
        Identity identity = Identity.builder().id(1L).login(USERNAME).password(PASSWORD).name("identityName").build();
        Environnement environnement = Environnement.builder().id(1L).identity(identity).host("localhost").norme(norme).build();
        RetourBatch retourBatch = shellService.runCommandNew(environnement, "echo hello");
        assertEquals("hello", retourBatch.getLogOut().trim());
        assertEquals(0, retourBatch.getReturnCode());
        assertTrue(retourBatch.getExecutionTime() > 0);
    }

    @Test
    public void listFile() throws EbadServiceException, IOException {
        Path tempFile = Files.createTempFile("UPLOAD_TEST", ".csv");
        FileWriter fileWriter = new FileWriter(tempFile.toAbsolutePath().toString());
        String testStr = "this is a test";
        fileWriter.append(testStr);
        fileWriter.flush();
        fileWriter.close();

        Norme norme = Norme.builder().commandLine("$1").build();
        Identity identity = Identity.builder().id(1L).login(USERNAME).password(PASSWORD).name("identityName").build();
        Environnement environnement = Environnement.builder().homePath("").id(1L).identity(identity).host("localhost").norme(norme).build();
        Directory directory = new Directory();
        directory.setPath("");
        directory.setEnvironnement(environnement);
        List<SftpClient.DirEntry> dirEntries = shellService.getListFiles(directory, "");
        assertEquals(1, dirEntries.size());
        assertEquals(".", dirEntries.get(0).getFilename());

        InputStream is = new FileInputStream(tempFile.toAbsolutePath().toString());
        shellService.uploadFile(directory, is, "test.csv", "");
        List<SftpClient.DirEntry> dirEntries2 = shellService.getListFiles(directory, "");
        assertEquals(2, dirEntries2.size());
        assertEquals("test.csv", dirEntries2.get(1).getFilename());

        InputStream resInputStream = shellService.getFile(directory, "test.csv", "");
        BufferedReader reader = new BufferedReader(new InputStreamReader(resInputStream));
        assertEquals(testStr, reader.readLine());

        shellService.removeFile(directory, "test.csv", "");
        List<SftpClient.DirEntry> dirEntries3 = shellService.getListFiles(directory, "");
        assertEquals(1, dirEntries3.size());
    }

    @Test
    public void startShell() throws EbadServiceException, IOException {
        Norme norme = Norme.builder().commandLine("$1").build();
        Identity identity = Identity.builder().id(1L).login(USERNAME).password(PASSWORD).name("identityName").build();
        Environnement environnement = Environnement.builder().homePath("").id(1L).identity(identity).host("localhost").norme(norme).build();
        UUID uuid = UUID.randomUUID();

        User user = User.builder().login("test").build();
        when(terminalRepository.getById(uuid)).thenReturn(Terminal.builder().environment(environnement).user(user).id(uuid).build());

        ChannelShell channelShell = shellService.startShell(uuid.toString());
        assertNotNull(channelShell);
    }

    @Test
    public void terminal() throws EbadServiceException, IOException, InterruptedException {
        Norme norme = Norme.builder().commandLine("$1").build();
        Identity identity = Identity.builder().id(1L).login(USERNAME).password(PASSWORD).name("identityName").build();
        Environnement environnement = Environnement.builder().homePath("").id(1L).identity(identity).host("localhost").norme(norme).build();
        UUID uuid = UUID.randomUUID();

        User user = User.builder().login("user").build();
        when(terminalRepository.getById(uuid)).thenReturn(Terminal.builder().environment(environnement).user(user).id(uuid).build());

        ChannelShell channelShell = shellService.startShell(uuid.toString());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                shellService.terminal("user",uuid.toString());
                verify(messagingTemplate).convertAndSendToUser(eq("user"), eq("/queue/terminal-" + uuid), anyString());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        Thread.sleep(1000);
        OutputStream outputStream = channelShell.getInvertedIn();
        outputStream.write("echo hello\n".getBytes());
        outputStream.flush();
        outputStream.write("exit\n".getBytes());
        outputStream.flush();
        Thread.sleep(1000);
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}
