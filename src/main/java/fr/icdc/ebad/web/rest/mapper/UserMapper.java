package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.web.rest.dto.UserDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(UserDto.class, User.class)
                .byDefault()
                .exclude("applications")
                .register();
    }
}
