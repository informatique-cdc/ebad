package fr.icdc.ebad.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.util.RetourBatch;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
//@RunWith(SpringRunner.class)
@SpringBootTest
@PrepareForTest(ShellService.class)
@PowerMockIgnore("javax.management.*")
public class ShellServiceTest {
//    @Rule
//    public PowerMockRule rule = new PowerMockRule();

    @Autowired
    private ShellService shellService;
    @Mock
    private JSch jSch;
    @Mock
    private Session session;
    @Mock
    private ChannelExec channelExec;
    @Mock
    private ChannelSftp channelSftp;
    @Mock
    private EbadProperties ebadProperties;


    @Test
    public void runCommand() throws Exception {
        EbadProperties.SshProperties sshProperties = new EbadProperties.SshProperties();
        sshProperties.setPrivateKeyPath("/key");
        sshProperties.setPrivateKeyPassphrase("test");

        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);

        when(ebadProperties.getSsh()).thenReturn(sshProperties);
        doNothing().when(jSch).addIdentity(eq("/key"), eq("test"));
        when(jSch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);

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
        EbadProperties.SshProperties sshProperties = new EbadProperties.SshProperties();
        sshProperties.setPrivateKeyPath("/key");
        sshProperties.setPrivateKeyPassphrase("test");
        sshProperties.setLogin("ebad");

        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);

        when(ebadProperties.getSsh()).thenReturn(sshProperties);
        doNothing().when(jSch).addIdentity(eq("/key"), eq("test"));
        when(jSch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);

        Vector<ChannelSftp.LsEntry> lsEntries = createEntries();
        when(channelSftp.ls(eq("/home/dir"))).thenReturn(lsEntries);

        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);
        List<ChannelSftp.LsEntry> results = shellService.getListFiles(directory);

        assertEquals(lsEntries, results);
    }

    @Test
    public void removeFile() throws Exception {
        EbadProperties.SshProperties sshProperties = new EbadProperties.SshProperties();
        sshProperties.setPrivateKeyPath("/key");
        sshProperties.setPrivateKeyPassphrase("test");
        sshProperties.setLogin("ebad");

        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);

        when(ebadProperties.getSsh()).thenReturn(sshProperties);
        doNothing().when(jSch).addIdentity(eq("/key"), eq("test"));
        when(jSch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);


        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);
        shellService.removeFile(directory, "test.txt");

        verify(channelSftp).rm(eq("/home/dir/test.txt"));

    }

    @Test
    public void getFile() throws Exception {
        EbadProperties.SshProperties sshProperties = new EbadProperties.SshProperties();
        sshProperties.setPrivateKeyPath("/key");
        sshProperties.setPrivateKeyPassphrase("test");
        sshProperties.setLogin("ebad");

        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);

        when(ebadProperties.getSsh()).thenReturn(sshProperties);
        doNothing().when(jSch).addIdentity(eq("/key"), eq("test"));
        when(jSch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);

        Vector<ChannelSftp.LsEntry> lsEntries = createEntries();
        when(channelSftp.ls(eq("/home/dir"))).thenReturn(lsEntries);

        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);

        InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode("test").array());
        when(channelSftp.get(eq("/home/dir/test.txt"), any(SftpProgressMonitor.class))).thenReturn(is);

        InputStream result = shellService.getFile(directory, "test.txt");

        verify(channelSftp).get(eq("/home/dir/test.txt"), any(SftpProgressMonitor.class));
        String resultStr = IOUtils.toString(result, StandardCharsets.UTF_8.name());
        assertEquals("test", resultStr);
    }

    @Test
    public void uploadFile() throws Exception {
        EbadProperties.SshProperties sshProperties = new EbadProperties.SshProperties();
        sshProperties.setPrivateKeyPath("/key");
        sshProperties.setPrivateKeyPassphrase("test");
        sshProperties.setLogin("ebad");

        PowerMockito.whenNew(JSch.class).withNoArguments().thenReturn(jSch);

        when(ebadProperties.getSsh()).thenReturn(sshProperties);
        doNothing().when(jSch).addIdentity(eq("/key"), eq("test"));
        when(jSch.getSession(eq("ebad"), eq("localhost"), eq(22))).thenReturn(session);
        when(session.openChannel(eq("sftp"))).thenReturn(channelSftp);

        Vector<ChannelSftp.LsEntry> lsEntries = createEntries();
        when(channelSftp.ls(eq("/home/dir"))).thenReturn(lsEntries);

        Norme norme = Norme.builder().commandLine("/bin/bash $1").build();
        Environnement environnement = Environnement.builder().id(1L).host("localhost").norme(norme).homePath("/home").build();
        Directory directory = new Directory();
        directory.setPath("dir");
        directory.setEnvironnement(environnement);

        InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode("test").array());

        shellService.uploadFile(directory, is, "test.txt");

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
        when(attributes.getSize()).thenReturn(size);
        when(attributes.getMTime()).thenReturn(mTime);

        ChannelSftp.LsEntry entry = mock(ChannelSftp.LsEntry.class);
        when(entry.getAttrs()).thenReturn(attributes);
        when(entry.getFilename()).thenReturn(fileName);
        when(entry.getAttrs().isDir()).thenReturn(directory);

        return entry;
    }
}
