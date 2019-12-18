package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.repository.ActualiteRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActualiteServiceTest {
    @InjectMocks
    private ActualiteService actualiteService;

    @Mock
    private ActualiteRepository actualiteRepository;

    @Test
    public void getAllActualites() {
        List<Actualite> actualiteList = new ArrayList<>();

        Actualite actualite1 = Actualite.builder().id(1L).build();
        actualiteList.add(actualite1);
        Actualite actualite2 = Actualite.builder().id(2L).build();
        actualiteList.add(actualite2);
        Actualite actualite3 = Actualite.builder().id(3L).build();
        actualiteList.add(actualite3);
        Actualite actualite4 = Actualite.builder().id(4L).build();
        actualiteList.add(actualite4);

        when(actualiteRepository.findAll(any(Sort.class))).thenReturn(actualiteList);
        List<Actualite> result = actualiteService.getAllActualites();

        assertEquals(4, result.size(), 0);
    }

    @Test
    public void getAllActualitesPubliees() {
        List<Actualite> actualiteList = new ArrayList<>();

        Actualite actualite1 = Actualite.builder().id(1L).build();
        actualiteList.add(actualite1);
        Actualite actualite2 = Actualite.builder().id(2L).build();
        actualiteList.add(actualite2);
        Actualite actualite3 = Actualite.builder().id(3L).build();
        actualiteList.add(actualite3);
        Actualite actualite4 = Actualite.builder().id(4L).build();
        actualiteList.add(actualite4);

        when(actualiteRepository.findByDraftFalse(any(Sort.class))).thenReturn(actualiteList);
        List<Actualite> result = actualiteService.getAllActualitesPubliees();

        assertEquals(4, result.size(), 0);
    }

    @Test
    public void getActualite() {
        Actualite actualite1 = Actualite.builder().id(1L).build();

        when(actualiteRepository.findById(eq(1L))).thenReturn(Optional.of(actualite1));
        Optional<Actualite> result = actualiteService.getActualite(1L);

        assertEquals(1L, result.get().getId(), 0);
    }

    @Test
    public void saveActualite() {
        Actualite actualite1 = Actualite.builder().id(1L).build();
        actualiteService.saveActualite(actualite1);
        verify(actualiteRepository).save(eq(actualite1));
    }

    @Test
    public void deleteActualite() {
        Actualite actualite1 = Actualite.builder().id(1L).build();
        actualiteService.deleteActualite(actualite1);
        verify(actualiteRepository).deleteById(eq(actualite1.getId()));
    }
}
