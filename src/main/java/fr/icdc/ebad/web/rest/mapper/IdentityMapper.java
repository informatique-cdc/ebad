package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.web.rest.dto.CompleteIdentityDto;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.Type;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class IdentityMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        ConverterFactory converterFactory = orikaMapperFactory.getConverterFactory();
        converterFactory.registerConverter("applicationLongIdConverter", new ApplicationLongIdConverter());

        orikaMapperFactory.classMap(Identity.class, CompleteIdentityDto.class)
                .fieldMap("availableApplication", "availableApplication").converter("applicationLongIdConverter").add()
                .byDefault()
                .register();
        orikaMapperFactory.classMap(CompleteIdentityDto.class, Identity.class)
                .fieldMap("availableApplication", "availableApplication").converter("applicationLongIdConverter").add()
                .byDefault()
                .register();
    }

    class ApplicationLongIdConverter extends BidirectionalConverter<Application,Long> {

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


