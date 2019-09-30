package fr.icdc.ebad.service.mapper;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.plugin.dto.NormeDiscoverDto;
import ma.glasnost.orika.MapperFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
public class NormeDiscoverDtoTest {
    @Autowired
    MapperFacade mapperFacade;

    @Test
    public void normeDiscoverDtoToNormeTest() {
        NormeDiscoverDto normeDiscoverDto = new NormeDiscoverDto();
        normeDiscoverDto.setCommandLine("$1");
        normeDiscoverDto.setId(1L);
        normeDiscoverDto.setName("Linux");
        normeDiscoverDto.setFileDate("date.txt");
        normeDiscoverDto.setPathShellDirectory("shell");

        Norme norme = mapperFacade.map(normeDiscoverDto, Norme.class);

        assertEquals(normeDiscoverDto.getId(), norme.getId());
        assertEquals(normeDiscoverDto.getCommandLine(), norme.getCommandLine());
        assertEquals(normeDiscoverDto.getName(), norme.getName());
        assertEquals(normeDiscoverDto.getFileDate(), norme.getCtrlMDate());
        assertEquals(normeDiscoverDto.getPathShellDirectory(), norme.getPathShell());
    }

    @Test
    public void normeTonormeDiscoverDtoTest() {
        Norme norme = new Norme();
        norme.setCommandLine("$1");
        norme.setId(1L);
        norme.setName("Linux");
        norme.setCtrlMDate("date.txt");
        norme.setPathShell("shell");

        NormeDiscoverDto normeDiscoverDto = mapperFacade.map(norme, NormeDiscoverDto.class);

        assertEquals(norme.getId(), normeDiscoverDto.getId());
        assertEquals(norme.getCommandLine(), normeDiscoverDto.getCommandLine());
        assertEquals(norme.getName(), normeDiscoverDto.getName());
        assertEquals(norme.getCtrlMDate(), normeDiscoverDto.getFileDate());
        assertEquals(norme.getPathShell(), normeDiscoverDto.getPathShellDirectory());
    }
}
