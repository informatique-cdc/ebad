package fr.icdc.ebad.config;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component
public class MapperFactoryConfig implements FactoryBean<MapperFactory> {

    @Override
    public MapperFactory getObject() {
        return new DefaultMapperFactory.Builder().unenhanceStrategy(new HibernateUnenhanceStrategy()).build();
    }

    @Override
    public Class<?> getObjectType() {
        return MapperFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
