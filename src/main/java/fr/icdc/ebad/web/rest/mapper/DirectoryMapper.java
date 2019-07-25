package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.web.rest.dto.DirectoryDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class DirectoryMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(Directory.class, DirectoryDto.class)
                .byDefault()
                .register();
    }
}
