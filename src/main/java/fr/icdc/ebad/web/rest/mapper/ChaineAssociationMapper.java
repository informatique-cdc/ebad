package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.ChaineAssociation;
import fr.icdc.ebad.web.rest.dto.ChaineAssociationDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ChaineAssociationMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(ChaineAssociation.class, ChaineAssociationDto.class)
                .byDefault()
                .register();
    }
}
