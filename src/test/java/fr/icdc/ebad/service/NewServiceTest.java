package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.repository.NewRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NewServiceTest {
    @InjectMocks
    private NewService newService;

    @Mock
    private NewRepository newRepository;

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
        Page<Actualite> actualitePage = new PageImpl<>(actualiteList);

        when(newRepository.findAll(any(Pageable.class))).thenReturn(actualitePage);
        Page<Actualite> result = newService.getAllActualites(PageRequest.of(0, 10));

        assertEquals(4, result.getContent().size(), 0);
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

        Page<Actualite> actualitePage = new PageImpl<>(actualiteList);
        when(newRepository.findByDraftFalse(any(Pageable.class))).thenReturn(actualitePage);
        Page<Actualite> result = newService.getAllActualitesPubliees(PageRequest.of(0, 10));

        assertEquals(4, result.getContent().size(), 0);
    }

    @Test
    public void getActualite() {
        Actualite actualite1 = Actualite.builder().id(1L).build();

        when(newRepository.findById(eq(1L))).thenReturn(Optional.of(actualite1));
        Optional<Actualite> result = newService.getActualite(1L);

        assertEquals(1L, result.get().getId(), 0);
    }

    @Test
    public void saveActualite() {
        Actualite actualite1 = Actualite.builder().id(1L).build();
        newService.saveActualite(actualite1);
        verify(newRepository).save(eq(actualite1));
    }

    @Test
    public void deleteActualite() {
        Actualite actualite1 = Actualite.builder().id(1L).build();
        newService.deleteActualite(actualite1);
        verify(newRepository).deleteById(eq(actualite1.getId()));
    }
}
