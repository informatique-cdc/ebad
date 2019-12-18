package fr.icdc.ebad.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.FilesDto;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryServiceTest {
    @Mock
    private DirectoryRepository directoryRepository;
    @Mock
    private ShellService shellService;
    @InjectMocks
    private DirectoryService directoryService;

    @Test
    public void listAllFiles() throws SftpException, JSchException, EbadServiceException {
        Directory directory = Directory.builder().id(1L).build();
        List<ChannelSftp.LsEntry> files = new ArrayList<>();
        files.add(lsEntryWithGivenFilenameAndMTime("test1.txt", unixTimestampForDaysAgo(30)));
        files.add(lsEntryWithGivenFilenameAndMTime("test2.txt", unixTimestampForDaysAgo(30)));
        files.add(lsEntryWithGivenFilenameAndMTime("test3.txt", unixTimestampForDaysAgo(30)));
        when(directoryRepository.getOne(eq(1L))).thenReturn(directory);
        when(shellService.getListFiles(eq(directory))).thenReturn(files);
        List<FilesDto> result = directoryService.listAllFiles(1L);
        assertEquals(3, result.size());
    }

    @Test(expected = EbadServiceException.class)
    public void listAllFilesException() throws SftpException, JSchException, EbadServiceException {
        Directory directory = Directory.builder().id(1L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(directory);
        when(shellService.getListFiles(eq(directory))).thenThrow(new SftpException(1, "erreur test"));
        directoryService.listAllFiles(1L);
    }

    @Test(expected = IllegalAccessError.class)
    public void removeFile() throws SftpException, JSchException, EbadServiceException {
        Directory directory = Directory.builder().id(1L).name("test").canWrite(true).build();
        FilesDto filesDTO = new FilesDto();
        filesDTO.setDirectory(directory);
        when(directoryRepository.getOne(1L)).thenReturn(directory);
        doNothing().when(shellService).removeFile(eq(directory), eq(filesDTO.getName()));
        directoryService.removeFile(filesDTO);

        directory.setCanWrite(false);
        when(directoryRepository.getOne(1L)).thenReturn(directory);
        directoryService.removeFile(filesDTO);
    }

    @Test(expected = EbadServiceException.class)
    public void readFile() throws SftpException, JSchException, IOException, EbadServiceException {
        InputStream inputStream = IOUtils.toInputStream("hello", Charset.forName("UTF-8"));
        Directory directory = Directory.builder().id(1L).name("test").canWrite(true).build();
        FilesDto filesDTO = new FilesDto();
        filesDTO.setDirectory(directory);
        when(directoryRepository.getOne(1L)).thenReturn(directory);
        when(shellService.getFile(eq(directory), eq(filesDTO.getName()))).thenReturn(inputStream);
        InputStream result = directoryService.readFile(filesDTO);
        assertEquals(inputStream, result);
        when(shellService.getFile(eq(directory), eq(filesDTO.getName()))).thenThrow(new SftpException(1, "test"));
        directoryService.readFile(filesDTO);
    }


    @Test(expected = EbadServiceException.class)
    public void uploadFile() throws SftpException, JSchException, EbadServiceException {
        InputStream inputStream = IOUtils.toInputStream("hello", Charset.forName("UTF-8"));

        Directory directory = Directory.builder().id(1L).name("test").canWrite(true).build();
        FilesDto filesDTO = new FilesDto();
        filesDTO.setDirectory(directory);
        doNothing().when(shellService).uploadFile(eq(directory), eq(inputStream), eq(filesDTO.getName()));
        directoryService.uploadFile(inputStream, filesDTO);
        verify(shellService).uploadFile(eq(directory), eq(inputStream), eq(filesDTO.getName()));

        directory.setCanWrite(false);
        doThrow(new SftpException(1, "test")).when(shellService).uploadFile(eq(directory), eq(inputStream), eq(filesDTO.getName()));
        directoryService.uploadFile(inputStream, filesDTO);
    }

    @Test(expected = IllegalAccessError.class)
    public void uploadFileKo() throws EbadServiceException {
        InputStream inputStream = IOUtils.toInputStream("hello", Charset.forName("UTF-8"));
        FilesDto filesDTO = new FilesDto();
        directoryService.uploadFile(inputStream, filesDTO);
    }

    private ChannelSftp.LsEntry lsEntryWithGivenFilenameAndMTime(String filename, long mtime) {
        ChannelSftp.LsEntry lsEntry = mock(ChannelSftp.LsEntry.class);
        SftpATTRS attrs = mock(SftpATTRS.class);
        when(lsEntry.getAttrs()).thenReturn(attrs);
        when(lsEntry.getFilename()).thenReturn(filename);
        when(attrs.getMTime()).thenReturn((int) mtime);
        return lsEntry;
    }

    private long unixTimestampForDaysAgo(int days) {
        return new DateTime().minusDays(days).getMillis() / 1000;
    }

    @Test
    public void testGetDirectory() {
        Directory directory = new Directory();
        directory.setId(1L);
        when(directoryRepository.getOne(eq(directory.getId()))).thenReturn(directory);

        Directory result = directoryService.getDirectory(directory.getId());

        assertEquals(directory, result);
    }

    @Test
    public void testfindDirectoryFromEnvironnement() {
        List<Directory> directoryList = new ArrayList<>();
        Directory directory1 = new Directory();
        directory1.setId(1L);
        directoryList.add(directory1);

        Directory directory2 = new Directory();
        directory2.setId(2L);
        directoryList.add(directory2);

        Page<Directory> directoryPage = new PageImpl<>(directoryList, Pageable.unpaged(), 2L);

        Environnement environnement = new Environnement();
        environnement.setId(1L);
        when(directoryRepository.findDirectoryFromEnvironnement(eq(Pageable.unpaged()), eq(environnement.getId()))).thenReturn(directoryPage);

        Page<Directory> result = directoryService.findDirectoryFromEnvironnement(environnement.getId(), Pageable.unpaged());

        assertEquals(directoryPage, result);
        assertEquals(directoryList, result.getContent());
        assertEquals(directoryList.size(), result.getContent().size());
    }

    @Test
    public void testSaveDirectory() {
        Directory directory = new Directory();
        directory.setId(1L);

        when(directoryRepository.save(eq(directory))).thenReturn(directory);

        Directory result = directoryService.saveDirectory(directory);

        verify(directoryRepository).save(eq(directory));
        assertEquals(directory, result);
    }

    @Test
    public void deleteDirectory() {
        directoryService.deleteDirectory(1L);
        verify(directoryRepository).deleteById(eq(1L));
    }
}
