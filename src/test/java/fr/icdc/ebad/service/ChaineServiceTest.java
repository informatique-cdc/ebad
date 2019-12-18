package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.ChaineAssociation;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.ChaineRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dtrouillet on 15/03/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChaineServiceTest {

    @Mock
    private BatchService batchService;

    @Mock
    private ChaineRepository chaineRepository;

    @InjectMocks
    private ChaineService chaineService;


    @Test
    public void runChaine() throws Exception {
        Environnement environnement = new Environnement();
        environnement.setId(4L);

        Batch batch1 = new Batch();
        batch1.setId(1L);

        Batch batch2 = new Batch();
        batch2.setId(2L);

        Batch batch3 = new Batch();
        batch3.setId(3L);

        ChaineAssociation chaineAssociation1 = new ChaineAssociation();
        chaineAssociation1.setBatch(batch1);
        chaineAssociation1.setBatchOrder(0);

        ChaineAssociation chaineAssociation2 = new ChaineAssociation();
        chaineAssociation2.setBatch(batch2);
        chaineAssociation2.setBatchOrder(1);

        ChaineAssociation chaineAssociation3 = new ChaineAssociation();
        chaineAssociation3.setBatch(batch3);
        chaineAssociation3.setBatchOrder(2);

        Chaine chaine = new Chaine();
        chaine.getChaineAssociations().add(chaineAssociation1);
        chaine.getChaineAssociations().add(chaineAssociation2);
        chaine.getChaineAssociations().add(chaineAssociation3);
        chaine.setEnvironnement(environnement);

        RetourBatch retourBatch1 = new RetourBatch("ok1", 0, 100L);
        RetourBatch retourBatch2 = new RetourBatch("ko1", 1, 200L);

        when(batchService.runBatch(eq(batch1), eq(environnement))).thenReturn(retourBatch1);
        when(batchService.runBatch(eq(batch2), eq(environnement))).thenReturn(retourBatch2);

        chaineService.runChaine(chaine);

        verify(batchService, times(1)).runBatch(eq(batch1), eq(environnement));
        verify(batchService, times(1)).runBatch(eq(batch2), eq(environnement));
        verify(batchService, times(0)).runBatch(eq(batch3), eq(environnement));
    }

    @Test
    public void runChaine2() throws Exception {
        Environnement environnement = new Environnement();
        environnement.setId(4L);

        Batch batch1 = new Batch();
        batch1.setId(1L);

        Batch batch2 = new Batch();
        batch2.setId(2L);

        Batch batch3 = new Batch();
        batch3.setId(3L);

        ChaineAssociation chaineAssociation1 = new ChaineAssociation();
        chaineAssociation1.setBatch(batch1);
        chaineAssociation1.setBatchOrder(0);

        ChaineAssociation chaineAssociation2 = new ChaineAssociation();
        chaineAssociation2.setBatch(batch2);
        chaineAssociation2.setBatchOrder(1);

        ChaineAssociation chaineAssociation3 = new ChaineAssociation();
        chaineAssociation3.setBatch(batch3);
        chaineAssociation3.setBatchOrder(2);

        Chaine chaine = new Chaine();
        chaine.getChaineAssociations().add(chaineAssociation1);
        chaine.getChaineAssociations().add(chaineAssociation2);
        chaine.getChaineAssociations().add(chaineAssociation3);
        chaine.setEnvironnement(environnement);

        RetourBatch retourBatch1 = new RetourBatch("ok1", 0, 100L);
        RetourBatch retourBatch2 = new RetourBatch("ko1", 1, 200L);

        when(batchService.runBatch(eq(batch1), eq(environnement))).thenReturn(retourBatch1);
        when(batchService.runBatch(eq(batch2), eq(environnement))).thenReturn(retourBatch2);

        when(chaineService.getChaine(1L)).thenReturn(chaine);
        chaineService.runChaine(1L);

        verify(batchService, times(1)).runBatch(eq(batch1), eq(environnement));
        verify(batchService, times(1)).runBatch(eq(batch2), eq(environnement));
        verify(batchService, times(0)).runBatch(eq(batch3), eq(environnement));
    }

    @Test
    public void addChaine() {
        List<ChaineAssociation> chaineAssociationList = new ArrayList<>();

        Batch batch1 = new Batch();
        batch1.setId(1L);

        ChaineAssociation chaineAssociation1 = new ChaineAssociation();
        chaineAssociation1.setBatchOrder(1);
        chaineAssociation1.setBatch(batch1);
        chaineAssociationList.add(chaineAssociation1);
        Batch batch2 = new Batch();
        batch2.setId(2L);

        ChaineAssociation chaineAssociation2 = new ChaineAssociation();
        chaineAssociation2.setBatchOrder(2);
        chaineAssociation2.setBatch(batch2);
        chaineAssociationList.add(chaineAssociation2);
        Chaine chaine = new Chaine();

        chaine.setId(5L);
        chaine.setChaineAssociations(chaineAssociationList);

        when(chaineRepository.saveAndFlush(eq(chaine))).thenReturn(chaine);
        when(batchService.getBatch(eq(1L))).thenReturn(batch1);
        when(batchService.getBatch(eq(2L))).thenReturn(batch2);

        Chaine result = chaineService.addChaine(chaine);

        verify(chaineRepository).saveAndFlush(eq(chaine));
        assertEquals(chaine, result);
    }

    @Test
    public void testGetChaine() {
        Chaine chaine = new Chaine();
        chaine.setId(1L);
        when(chaineRepository.getOne(chaine.getId())).thenReturn(chaine);

        Chaine result = chaineService.getChaine(chaine.getId());

        assertEquals(chaine, result);
    }

    @Test
    public void testGetAllChaineFromEnvironmentWithPageable() {
        List<Chaine> chaineList = new ArrayList<>();
        Chaine chaine1 = new Chaine();
        chaine1.setId(1L);
        chaineList.add(chaine1);

        Chaine chaine2 = new Chaine();
        chaine2.setId(2L);
        chaineList.add(chaine2);

        Page<Chaine> chainePage = new PageImpl<>(chaineList, Pageable.unpaged(), 2L);

        Environnement environnement = new Environnement();
        environnement.setId(1L);
        when(chaineRepository.findChaineFromEnvironnement(eq(Pageable.unpaged()), eq(environnement.getId()))).thenReturn(chainePage);

        Page<Chaine> result = chaineService.getAllChaineFromEnvironmentWithPageable(environnement, Pageable.unpaged());

        assertEquals(chainePage, result);
        assertEquals(chaineList, result.getContent());
        assertEquals(chaineList.size(), result.getContent().size());
    }

    @Test
    public void testDeleteChaine() {
        chaineService.deleteChaine(1L);
        verify(chaineRepository).deleteById(eq(1L));
    }

    @Test
    public void testSaveChaine() {
        Chaine chaine = new Chaine();
        chaine.setId(1L);
        when(chaineRepository.save(eq(chaine))).thenReturn(chaine);

        Chaine result = chaineService.saveChaine(chaine);

        assertEquals(chaine, result);
    }

}
