package fr.icdc.ebad.web.rest.mapper;

import dev.akkinoc.spring.boot.orika.OrikaMapperFactoryConfigurer;
import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.web.rest.dto.NotificationDto;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.Type;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        ConverterFactory converterFactory = orikaMapperFactory.getConverterFactory();
        converterFactory.registerConverter("notificationDateCreatedConverter", new NotificationDateCreatedConverter());

        orikaMapperFactory.classMap(Notification.class, NotificationDto.class)
                .fieldMap("createdDate", "createdDate").converter("notificationDateCreatedConverter").add()
                .byDefault()
                .register();
    }

    class NotificationDateCreatedConverter extends CustomConverter<DateTime, DateTime> {
        @Override
        public DateTime convert(DateTime createdDate, Type<? extends DateTime> type, MappingContext mappingContext) {
            return createdDate;
        }
    }
}


