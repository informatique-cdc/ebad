package fr.icdc.ebad.web.rest.mapper;

import dev.akkinoc.spring.boot.orika.OrikaMapperFactoryConfigurer;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.web.rest.dto.CompleteIdentityDto;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

@Component
public class IdentityMapper implements OrikaMapperFactoryConfigurer {
    private static final String CONVERTER_NAME = "applicationLongIdConverter";
    private static final String AVAILABLE_APPLICATION_FIELD = "availableApplication";
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        ConverterFactory converterFactory = orikaMapperFactory.getConverterFactory();
        converterFactory.registerConverter(CONVERTER_NAME, new ApplicationLongIdConverter());

        orikaMapperFactory.classMap(Identity.class, CompleteIdentityDto.class)
                .fieldMap(AVAILABLE_APPLICATION_FIELD, AVAILABLE_APPLICATION_FIELD).converter(CONVERTER_NAME).add()
                .byDefault()
                .register();
        orikaMapperFactory.classMap(CompleteIdentityDto.class, Identity.class)
                .fieldMap(AVAILABLE_APPLICATION_FIELD, AVAILABLE_APPLICATION_FIELD).converter(CONVERTER_NAME).add()
                .byDefault()
                .register();
    }

    static class ApplicationLongIdConverter extends BidirectionalConverter<Application,Long> {

        @Override
        public Long convertTo(Application application, Type<Long> type, MappingContext mappingContext) {
            if (application == null) {
                return null;
            }
            return application.getId();
        }

        @Override
        public Application convertFrom(Long aLong, Type<Application> type, MappingContext mappingContext) {
            if (aLong == null) {
                return null;
            }
            return Application.builder().id(aLong).build();
        }
    }


}


