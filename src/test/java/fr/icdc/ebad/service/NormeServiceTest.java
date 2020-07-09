package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.QNorme;
import fr.icdc.ebad.repository.NormeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NormeServiceTest {
    @Mock
    private NormeRepository normeRepository;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private NormeService normeService;

    @Test
    public void getAllNormesSorted() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        List<Norme> normeList = new ArrayList<>();
        Norme norme1 = Norme.builder().id(1L).build();
        Norme norme2 = Norme.builder().id(2L).build();
        normeList.add(norme1);
        normeList.add(norme2);
        Page<Norme> normePage = new PageImpl<>(normeList);
        when(normeRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(normePage);

        Page<Norme> results = normeService.getAllNormes(QNorme.norme.id.eq(1L), pageable);

        assertEquals(normeList.size(), results.getContent().size());
        assertTrue(results.getContent().contains(norme1));
        assertTrue(results.getContent().contains(norme2));
    }

    @Test
    public void findNormeById() {
        Norme norme1 = Norme.builder().id(1L).build();
        when(normeRepository.findById(eq(1L))).thenReturn(Optional.of(norme1));

        Optional<Norme> result = normeService.findNormeById(1L);

        assertTrue(result.isPresent());
        assertEquals(norme1, result.get());
    }

    @Test
    public void saveNorme() {
        Norme norme1 = Norme.builder().id(1L).build();
        when(normeRepository.saveAndFlush(eq(norme1))).thenReturn(norme1);

        Norme result = normeService.saveNorme(norme1);

        verify(normeRepository).saveAndFlush(eq(norme1));
        assertEquals(norme1, result);
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
