package fr.icdc.ebad.service.jpa;

import com.querydsl.core.types.dsl.BooleanExpression;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.QBatch;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.UsageApplicationId;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.service.BatchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
@Transactional
public class BatchServiceJpaTest {
    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private BatchService batchService;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser(username = "toto")
    public void testGetAllBatchWithPredicate() {
        Authority authority = new Authority();
        authority.setName("ROLE_USER");

        User user = User.builder()
                .login("toto")
                .password("password")
                .build();
        user.getAuthorities().add(authority);
        entityManager.persist(user);

        Application application1 = Application.builder().name("MyApp").code("CCC").build();
        entityManager.persist(application1);

        Application application2 = Application.builder().name("MyApp2").code("DDD").build();
        entityManager.persist(application2);

        Norme norme = Norme.builder().name("UNIX").commandLine("$1").pathShell("/").ctrlMDate("ctr").build();
        entityManager.persist(norme);

        Environnement environnement1 = Environnement.builder().application(application1)
                .login("login")
                .norme(norme)
                .host("localhost")
                .prefix("")
                .name("dev").build();
        entityManager.persist(environnement1);

        Environnement environnement2 = Environnement.builder().application(application1)
                .login("login")
                .norme(norme)
                .host("localhost")
                .prefix("")
                .name("int").build();
        entityManager.persist(environnement2);

        UsageApplication usageApplication = UsageApplication
                .builder()
                .application(application1)
                .usageApplicationId(new UsageApplicationId(user.getId(), application1.getId()))
                .user(user).canManage(true)
                .canUse(false)
                .build();
        entityManager.persist(usageApplication);

        Batch batch1 = new Batch();
        batch1.setName("batch1");
        batch1.setPath("batch1.sh");
        batch1.getEnvironnements().add(environnement1);
        entityManager.persist(batch1);

        Batch batch2 = new Batch();
        batch2.setName("batch2");
        batch2.setPath("batch2.sh");
        batch1.getEnvironnements().add(environnement1);
        entityManager.persist(batch2);


        Batch batch3 = new Batch();
        batch3.setName("batch3");
        batch3.setPath("batch3.sh");
        batch3.getEnvironnements().add(environnement2);
        entityManager.persist(batch3);


        BooleanExpression predicate1 = QBatch.batch.environnements.any().application.id.eq(application1.getId());
        Page<Batch> batchPage1 = batchService.getAllBatchWithPredicate(predicate1, PageRequest.of(0, 10));
        assertEquals(2, batchPage1.getContent().size());

        BooleanExpression predicate2 = QBatch.batch.environnements.any().application.id.eq(application2.getId());
        Page<Batch> batchPage2 = batchService.getAllBatchWithPredicate(predicate2, PageRequest.of(0, 10));
        assertEquals(0, batchPage2.getContent().size());
    }
}
