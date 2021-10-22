package fr.icdc.ebad.service.mapper;

import dev.akkinoc.spring.boot.orika.OrikaMapperFactoryConfigurer;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.web.rest.dto.LogBatchDto;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Component;

@Component
public class LogBatchDtoMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(LogBatch.class, LogBatchDto.class)
                .field("user.login", "login")
                .byDefault()
                .register();
    }

}
