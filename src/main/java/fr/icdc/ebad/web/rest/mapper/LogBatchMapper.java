package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.web.rest.dto.LogBatchDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class LogBatchMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(LogBatch.class, LogBatchDto.class)
                .byDefault()
                .register();
    }
}
