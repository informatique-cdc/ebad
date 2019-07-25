package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.web.rest.dto.NormeDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class NormeMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(Norme.class, NormeDto.class)
                .byDefault()
                .register();
    }
}
