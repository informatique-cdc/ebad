package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.BatchEnvironnementDto;
import fr.icdc.ebad.web.rest.dto.DirectoryDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionDirectoryTest {
    @InjectMocks
    private PermissionDirectory permissionDirectory;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DirectoryRepository directoryRepository;

    @Mock
    private UserDetails userDetails;

    @Test
    public void canReadLong() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canRead(1L, userDetails);
        assertTrue(result);
    }

    @Test
    public void cantReadLong() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().environnement(environnement).build());
        boolean result = permissionDirectory.canRead(1L, userDetails);
        assertFalse(result);
    }

    @Test
    public void canReadSubDirectory() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canRead(1L, "", userDetails);
        assertTrue(result);
    }

    @Test
    public void canReadSubDirectory2() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().environnement(environnement).canExplore(true).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canRead(1L, "subDir", userDetails);
        assertTrue(result);
    }

    @Test
    public void cantReadSubDirectory() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(null);
        boolean result = permissionDirectory.canRead(1L, "", userDetails);
        assertFalse(result);
    }

    @Test
    public void cantReadSubDirectory1() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().environnement(environnement).canExplore(false).build());
        boolean result = permissionDirectory.canRead(1L, "subDir", userDetails);
        assertFalse(result);
    }

    @Test
    public void canRead() {
        DirectoryDto directoryDto = DirectoryDto.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canRead(directoryDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void canWrite() {
        BatchEnvironnementDto environnementDto = new BatchEnvironnementDto();
        environnementDto.setId(2L);
        DirectoryDto directoryDto = DirectoryDto.builder().id(1L).environnement(environnementDto).build();
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findManagerFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canWrite(directoryDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void cantWrite() {
        DirectoryDto directoryDto = DirectoryDto.builder().id(1L).build();
        boolean result = permissionDirectory.canWrite(directoryDto, userDetails);
        assertFalse(result);
    }

    @Test
    public void canWriteFile() {
        DirectoryDto directoryDto = DirectoryDto.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().canWrite(true).environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canWriteFile(directoryDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void canWriteFileLong() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().canWrite(true).environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canWriteFile(1L, userDetails);
        assertTrue(result);
    }

    @Test
    public void cantWriteFile() {
        DirectoryDto directoryDto = DirectoryDto.builder().id(1L).canWrite(true).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().canWrite(false).environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canWriteFile(directoryDto, userDetails);
        assertFalse(result);
    }

    @Test
    public void canWriteFileSubDir() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().canWrite(true).environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canWriteFile(1L, "", userDetails);
        assertTrue(result);
    }

    @Test
    public void canWriteFileSubDir2() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().canWrite(true).environnement(environnement).canExplore(true).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canWriteFile(1L, "subDir", userDetails);
        assertTrue(result);
    }

    @Test
    public void cantWriteFileSubDir() {
        DirectoryDto directoryDto = DirectoryDto.builder().id(1L).canWrite(true).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().canWrite(false).environnement(environnement).build());
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionDirectory.canWriteFile(1L, "", userDetails);
        assertFalse(result);
    }

    @Test
    public void cantWriteFileSubDir2() {
        Environnement environnement = Environnement.builder().id(2L).build();
        when(directoryRepository.getOne(eq(1L))).thenReturn(Directory.builder().canWrite(true).environnement(environnement).canExplore(false).build());
        boolean result = permissionDirectory.canWriteFile(1L, "subDir", userDetails);
        assertFalse(result);
    }

}
