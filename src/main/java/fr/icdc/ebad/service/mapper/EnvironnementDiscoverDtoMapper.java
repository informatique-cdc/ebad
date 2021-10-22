package fr.icdc.ebad.service.mapper;

import dev.akkinoc.spring.boot.orika.OrikaMapperFactoryConfigurer;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.plugin.dto.EnvironnementDiscoverDto;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Component;

@Component
public class EnvironnementDiscoverDtoMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(EnvironnementDiscoverDto.class, Environnement.class)
                .field("home", "homePath")
                .exclude("id")
                .byDefault()
                .register();
    }

}


