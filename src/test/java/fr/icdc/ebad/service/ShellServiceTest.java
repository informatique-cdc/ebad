package fr.icdc.ebad.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;
import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.util.RetourBatch;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

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
