package fr.icdc.ebad.config;

import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryBuilderConfigurer;
import org.springframework.stereotype.Component;

@Component
public class OrikaMapperFactoryBuilderConfiguration implements OrikaMapperFactoryBuilderConfigurer {
    @Override
    public void configure(DefaultMapperFactory.MapperFactoryBuilder<?, ?> orikaMapperFactoryBuilder) {
        orikaMapperFactoryBuilder.unenhanceStrategy(new HibernateUnenhanceStrategy());
    }
}
