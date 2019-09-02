package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.web.rest.dto.UsageApplicationSimpleDto;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.Type;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class UsageApplicationSimpleMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        ConverterFactory converterFactory = orikaMapperFactory.getConverterFactory();
        converterFactory.registerConverter("applicationToLongConverter", new ApplicationToLongConverter());

        orikaMapperFactory.classMap(UsageApplication.class, UsageApplicationSimpleDto.class)
                .fieldMap("application", "applicationId").converter("applicationToLongConverter").add()
                .byDefault()
                .register();
    }

    class ApplicationToLongConverter extends CustomConverter<Application, Long> {
        @Override
        public Long convert(Application application, Type<? extends Long> type, MappingContext mappingContext) {
            if (application == null) {
                return null;
            }
            return application.getId();
        }
    }
}


