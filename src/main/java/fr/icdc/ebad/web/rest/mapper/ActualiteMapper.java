package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.web.rest.dto.ActualiteDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ActualiteMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(Actualite.class, ActualiteDto.class)
                .byDefault()
                .register();
    }
}
