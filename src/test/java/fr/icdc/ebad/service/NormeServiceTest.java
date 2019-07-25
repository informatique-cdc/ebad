package fr.icdc.ebad.service;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.repository.NormeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class NormeServiceTest {
    @MockBean
    private NormeRepository normeRepository;

    @MockBean
    private ApplicationService applicationService;

    @Autowired
    private NormeService normeService;

    @Test
    public void getAllNormesSorted() {
        Sort sort = new Sort(Sort.Direction.ASC, "name");
        List<Norme> normeList = new ArrayList<>();
        Norme norme1 = Norme.builder().id(1L).build();
        Norme norme2 = Norme.builder().id(2L).build();
        normeList.add(norme1);
        normeList.add(norme2);

        when(normeRepository.findAll(eq(sort))).thenReturn(normeList);

        List<Norme> results = normeService.getAllNormesSorted(sort);

        assertEquals(normeList.size(), results.size());
        assertTrue(results.contains(norme1));
        assertTrue(results.contains(norme2));
    }

    @Test
    public void findNormeById() {
        Norme norme1 = Norme.builder().id(1L).build();
        when(normeRepository.findById(eq(1L))).thenReturn(Optional.of(norme1));

        Optional<Norme> result = normeService.findNormeById(1L);

        assertTrue(result.isPresent());
        assertEquals(result, result);
    }

    @Test
    public void saveNorme() {
        Norme norme1 = Norme.builder().id(1L).build();
        when(normeRepository.saveAndFlush(eq(norme1))).thenReturn(norme1);

        Norme result = normeService.saveNorme(norme1);

        verify(normeRepository).saveAndFlush(eq(norme1));
        assertEquals(result, result);
    }

    @Test
    public void deleteNormeById() {
        when(applicationService.findApplication(any())).thenReturn(new ArrayList<>());
        normeService.deleteNormeById(1L);
        verify(normeRepository).deleteById(eq(1L));
    }

    @Test(expected = IllegalStateException.class)
    public void deleteNormeByIdError() {
        List<Application> applicationList = new ArrayList<>();
        Application application1 = Application.builder().id(1L).build();
        applicationList.add(application1);

        when(applicationService.findApplication(any())).thenReturn(applicationList);

        normeService.deleteNormeById(1L);
    }
}
