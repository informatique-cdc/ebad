package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.QTypeFichier;
import fr.icdc.ebad.domain.TypeFichier;
import fr.icdc.ebad.repository.TypeFichierRepository;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TypeFichierServiceTest {

    @Mock
    private TypeFichierRepository typeFichierRepository;

    @InjectMocks
    private TypeFichierService typeFichierService;

    @Test
    public void getTypeFichierFromApplication() {
        Predicate predicate = QTypeFichier.typeFichier.id.eq(1L);
        Pageable pageable = PageRequest.of(0, 10);


        TypeFichier typeFichier1 = new TypeFichier();
        typeFichier1.setId(1L);
        TypeFichier typeFichier2 = new TypeFichier();
        typeFichier2.setId(2L);

        List<TypeFichier> typeFichiers = new ArrayList<>();
        typeFichiers.add(typeFichier1);
        typeFichiers.add(typeFichier2);

        Page<TypeFichier> typeFichierPage = new PageImpl<>(typeFichiers);
        when(typeFichierRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(typeFichierPage);

        Page<TypeFichier> result = typeFichierService.getTypeFichierFromApplication(predicate, pageable, 2L);

        assertEquals(typeFichierPage, result);
    }

    @Test
    public void saveTypeFichier() {
        TypeFichier typeFichier1 = new TypeFichier();
        typeFichier1.setName("testName");

        TypeFichier typeFichier2 = new TypeFichier();
        typeFichier2.setName("testName");
        typeFichier2.setId(1L);

        when(typeFichierRepository.save(eq(typeFichier1))).thenReturn(typeFichier2);
        TypeFichier result = typeFichierService.saveTypeFichier(typeFichier1);

        assertEquals(typeFichier2, result);
    }

    @Test
    public void deleteTypeFichier() {
        typeFichierService.deleteTypeFichier(1L);
        verify(typeFichierRepository).deleteById(eq(1L));
    }
}
