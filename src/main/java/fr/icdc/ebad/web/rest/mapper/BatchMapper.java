package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.web.rest.dto.BatchDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class BatchMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(Batch.class, BatchDto.class)
                .byDefault()
                .register();
    }
}
