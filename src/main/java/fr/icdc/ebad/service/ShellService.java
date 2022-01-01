package fr.icdc.ebad.service;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.apache.sshd.core.CoreModuleProperties;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Service
public class ShellService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellService.class);
    private static final String PATH_SEPARATOR = "/";
    private static final Duration HEARTBEAT = Duration.ofSeconds(2L);

    private final EbadProperties ebadProperties;
    private final IdentityService identityService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JobScheduler jobScheduler;
    private final ConcurrentHashMap<String,ChannelShell> channelsShell = new ConcurrentHashMap<>();

    public ShellService(EbadProperties ebadProperties, IdentityService identityService, SimpMessagingTemplate messagingTemplate, JobScheduler jobScheduler) {
        this.ebadProperties = ebadProperties;
        this.identityService = identityService;
        this.messagingTemplate = messagingTemplate;
        this.jobScheduler = jobScheduler;
    }

    public RetourBatch runCommandNew(Environnement environnement, String command) throws EbadServiceException {
        LOGGER.debug("run command {}", command);
        Long start = System.currentTimeMillis();
        int exitStatus;
        String commandOut;
        String commandErr;
        String commandWithInterpreteur = environnement.getNorme().getCommandLine().replace("$1", command);

        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        CoreModuleProperties.HEARTBEAT_INTERVAL.set(sshClient, HEARTBEAT);
        try (ClientSession session = createSession(sshClient, environnement)) {
            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ByteArrayOutputStream responseErrStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createChannel(Channel.CHANNEL_EXEC, commandWithInterpreteur)) {
                channel.setOut(responseStream);
                channel.setErr(responseErrStream);
                try {
                    channel.open();
                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), ebadProperties.getSsh().getTimeoutInMs());
                    commandOut = responseStream.toString();
                    commandErr = responseErrStream.toString();
                } finally {
                    exitStatus = channel.getExitStatus();
                    channel.close(true);
                }
            }
        } catch (IOException e) {
            throw new EbadServiceException("Error when trying to connect to the server", e);
        } finally {
            sshClient.stop();
        }

        Long end = System.currentTimeMillis();
        LOGGER.debug("Command out : {}", commandOut);
        LOGGER.debug("Command err : {}", commandErr);
        return new RetourBatch(commandOut, commandErr, exitStatus, end - start);
    }

    private static String constructSubDir(String originalSubDirectory) {
        String subDir = "";
        if (!StringUtils.isEmpty(originalSubDirectory)) {
            subDir = PATH_SEPARATOR + originalSubDirectory;
        }
        return subDir;
    }

    public List<SftpClient.DirEntry> getListFiles(Directory directory, String subDirectory) throws EbadServiceException {
        String path = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory);
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();

        try (ClientSession session = createSession(sshClient, directory.getEnvironnement())) {
            SftpClientFactory factory = SftpClientFactory.instance();

            try (SftpClient client = factory.createSftpClient(session)) {
                SftpClient.CloseableHandle handle = client.openDir(path);
                List<SftpClient.DirEntry> result = new ArrayList<>();
                client.listDir(handle).forEach(result::add);
                return result;
            }
        } catch (IOException e) {
            throw new EbadServiceException("Error when try to list file on the server", e);
        } finally {
            sshClient.stop();
        }
    }

    public void removeFile(Directory directory, String filename, String subDirectory) throws EbadServiceException {
        String path = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;

        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        try (ClientSession session = createSession(sshClient, directory.getEnvironnement())) {
            SftpClientFactory factory = SftpClientFactory.instance();

            try (SftpClient client = factory.createSftpClient(session)) {
                client.remove(path);
            }
        } catch (IOException e) {
            throw new EbadServiceException("Error when try to remove file on the server", e);
        } finally {
            sshClient.stop();
        }
    }

    public InputStream getFile(Directory directory, String filename, String subDirectory) throws EbadServiceException {
        String srcPath = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;

        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        try (ClientSession session = createSession(sshClient, directory.getEnvironnement())) {
            SftpClientFactory factory = SftpClientFactory.instance();

            try (SftpClient client = factory.createSftpClient(session);
                 InputStream inputStream = client.read(srcPath)) {

                File tmpFile = File.createTempFile("" + System.currentTimeMillis(), ".ebad-tmp");
                FileUtils.copyInputStreamToFile(inputStream, tmpFile);
                tmpFile.deleteOnExit();

                return new FileInputStream(tmpFile);
            }
        } catch (IOException e) {
            throw new EbadServiceException("Error when try to retrieve file on the server", e);
        } finally {
            sshClient.stop();
        }
    }

    private ClientSession createSession(SshClient sshClient, Environnement environnement) throws IOException, EbadServiceException {
        ClientSession session = sshClient
                .connect(
                        environnement.getIdentity().getLogin(),
                        environnement.getHost(),
                        ebadProperties.getSsh().getPort())
                .verify()
                .getSession();

        if (null != environnement.getIdentity().getPassword())
            session.addPasswordIdentity(environnement.getIdentity().getPassword());
        if (null != environnement.getIdentity().getPrivatekey() || null != environnement.getIdentity().getPrivatekeyPath())
            session.addPublicKeyIdentity(identityService.createKeyPair(environnement.getIdentity()));

        session.auth().verify();

        return session;
    }

    public void uploadFile(Directory directory, InputStream inputStream, String filename, String subDirectory) throws EbadServiceException {
        String dstPath = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;

        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        try (ClientSession session = createSession(sshClient, directory.getEnvironnement())) {
            SftpClientFactory factory = SftpClientFactory.instance();

            try (
                    SftpClient client = factory.createSftpClient(session);
                    SftpClient.CloseableHandle handle = client.open(dstPath, EnumSet.of(SftpClient.OpenMode.Write, SftpClient.OpenMode.Create))
            ) {
                int bufferSize = 1024 * 1024;
                byte[] src = new byte[bufferSize];
                int len;
                long fileOffset = 0L;
                while ((len = inputStream.read(src)) != -1) {
                    client.write(handle, fileOffset, src, 0, len);
                    fileOffset += len;
                }
            }
        } catch (IOException e) {
            throw new EbadServiceException("Error when try to upload file on the server", e);
        } finally {
            sshClient.stop();
        }
    }

    public ChannelShell startShell(Environnement environnement, String login, String idTerminal) throws IOException, EbadServiceException {
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        CoreModuleProperties.HEARTBEAT_INTERVAL.set(sshClient, Duration.ofSeconds(10));
        ClientSession session = createSession(sshClient, environnement);

        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream channelIn = new PipedInputStream(out);
        ChannelShell channel = session.createShellChannel();
        channel.setPtyType("xterm");
        channel.setIn(channelIn);

        channel.open().verify(Duration.ofSeconds(5));

        //with jobrunr
        channelsShell.put(idTerminal, channel);
        jobScheduler.enqueue(UUID.fromString(idTerminal), () -> terminal(login, idTerminal));


        return channel;
    }

    @Job(name = "Terminal", retries = 0)
    public void terminal(String login, String idTerminal) throws IOException {
        InputStream inputStream = channelsShell.get(idTerminal).getInvertedOut();
        try {
            byte[] buffer = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(buffer)) != -1) {
                byte[] message = Arrays.copyOfRange(buffer, 0, i);
                messagingTemplate.convertAndSendToUser(login, "/queue/terminal-"+idTerminal, message);
            }
        } finally {
            try {
                channelsShell.get(idTerminal).close();
                channelsShell.remove(idTerminal);
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
