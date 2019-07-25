package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(Application.class, ApplicationDto.class)
                .byDefault()
                .register();
    }
}
