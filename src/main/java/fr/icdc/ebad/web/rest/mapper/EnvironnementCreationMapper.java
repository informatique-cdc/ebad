package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.web.rest.dto.EnvironnementCreationDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class EnvironnementCreationMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(Environnement.class, EnvironnementCreationDto.class)
                .byDefault()
                .register();
    }
}
