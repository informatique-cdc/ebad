package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GlobalSettingRepository extends JpaRepository<GlobalSetting, String> {

}
