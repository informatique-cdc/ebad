package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.web.rest.dto.UserSimpleDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class UserSimpleMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(User.class, UserSimpleDto.class)
                .byDefault()
                .register();
    }
}
