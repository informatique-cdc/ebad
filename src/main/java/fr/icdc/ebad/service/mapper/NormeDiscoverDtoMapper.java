package fr.icdc.ebad.service.mapper;

import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.plugin.dto.NormeDiscoverDto;
import ma.glasnost.orika.MapperFactory;
import net.rakugakibox.spring.boot.orika.OrikaMapperFactoryConfigurer;
import org.springframework.stereotype.Component;

@Component
public class NormeDiscoverDtoMapper implements OrikaMapperFactoryConfigurer {
    @Override
    public void configure(MapperFactory orikaMapperFactory) {
        orikaMapperFactory.classMap(NormeDiscoverDto.class, Norme.class)
                .field("pathShellDirectory", "pathShell")
                .field("fileDate", "ctrlMDate")
                .byDefault()
                .register();
    }

}


