package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.web.rest.dto.NotificationDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(Notification.class, NotificationDto.class)
                .byDefault()
                .register();
    }
}
