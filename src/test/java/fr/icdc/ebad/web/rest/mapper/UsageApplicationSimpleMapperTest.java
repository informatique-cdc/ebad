package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.web.rest.dto.UsageApplicationSimpleDto;
import ma.glasnost.orika.MapperFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
public class UsageApplicationSimpleMapperTest {
    @Autowired
    MapperFacade mapperFacade;

    @Test
    public void applicationIdNonNullTest() {
        UsageApplication usageApplication = UsageApplication.builder().application(Application.builder().id(1L).build()).build();
        UsageApplicationSimpleDto result = mapperFacade.map(usageApplication, UsageApplicationSimpleDto.class);

        assertEquals(1L, result.getApplicationId(), 0);
    }

    @Test
    public void applicationIdNullTest() {
        UsageApplication usageApplication = UsageApplication.builder().application(Application.builder().build()).build();
        UsageApplicationSimpleDto result = mapperFacade.map(usageApplication, UsageApplicationSimpleDto.class);

        assertNull(result.getApplicationId());
    }
}
