package fr.icdc.ebad.web.rest.mapper;

import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.web.rest.dto.ChaineDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ChainMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(ChaineDto.class, Chaine.class)
                .byDefault()
                .field("environnement", "environnement")
                .register();
    }
}
