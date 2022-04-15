package fr.icdc.ebad.mapper;

import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.domain.K8SJob;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.domain.TypeFichier;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.plugin.dto.NormeDiscoverDto;
import fr.icdc.ebad.web.rest.dto.AccreditationRequestDto;
import fr.icdc.ebad.web.rest.dto.ActualiteDto;
import fr.icdc.ebad.web.rest.dto.ApiTokenDto;
import fr.icdc.ebad.web.rest.dto.ApiTokenWithKeyDto;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import fr.icdc.ebad.web.rest.dto.ApplicationSimpleDto;
import fr.icdc.ebad.web.rest.dto.BatchDto;
import fr.icdc.ebad.web.rest.dto.ChaineDto;
import fr.icdc.ebad.web.rest.dto.ChaineSimpleDto;
import fr.icdc.ebad.web.rest.dto.CompleteIdentityDto;
import fr.icdc.ebad.web.rest.dto.DirectoryDto;
import fr.icdc.ebad.web.rest.dto.EnvironnementCreationDto;
import fr.icdc.ebad.web.rest.dto.EnvironnementDto;
import fr.icdc.ebad.web.rest.dto.GlobalSettingKeyValueDto;
import fr.icdc.ebad.web.rest.dto.K8SJobDto;
import fr.icdc.ebad.web.rest.dto.LogBatchDto;
import fr.icdc.ebad.web.rest.dto.NormLabelIdDto;
import fr.icdc.ebad.web.rest.dto.NormeDto;
import fr.icdc.ebad.web.rest.dto.NotificationDto;
import fr.icdc.ebad.web.rest.dto.PublicIdentityDto;
import fr.icdc.ebad.web.rest.dto.SchedulingDto;
import fr.icdc.ebad.web.rest.dto.TypeFichierDto;
import fr.icdc.ebad.web.rest.dto.UsageApplicationDto;
import fr.icdc.ebad.web.rest.dto.UsageApplicationSimpleDto;
import fr.icdc.ebad.web.rest.dto.UserDto;
import fr.icdc.ebad.web.rest.dto.UserSimpleDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface MapStructMapper {
    //BATCHS
    BatchDto convert(Batch batch);

    Batch convert(BatchDto batchDto);

    //NEWS
    ActualiteDto convert(Actualite actualite);

    Actualite convert(ActualiteDto actualiteDto);

    //ACCREDITATIONS
    @Mapping(source = "user", target = "user", qualifiedByName = "UserSimpleDto")
    AccreditationRequestDto convert(AccreditationRequest accreditationRequest);

    AccreditationRequest convert(AccreditationRequestDto accreditationRequestDto);

    //API TOKEN
    ApiTokenDto convert(ApiToken apiToken);

    ApiToken convert(ApiTokenDto apiTokenDto);

    ApiTokenWithKeyDto convertToTokenWithKeyDto(ApiToken apiToken);

    //APPLICATIONS
    ApplicationSimpleDto convertToApplicationSimpleDto(Application application);

    ApplicationDto convertToApplicationDto(Application application);

    Application convert(ApplicationDto applicationDto);

    //USERS
    @IterableMapping(qualifiedByName = "UserSimpleDto")
    Set<UserSimpleDto> convertUserSimpleDtoSet(Set<User> users);

    @Named("UserSimpleDto")
    UserSimpleDto convertToUserSimpleDto(User users);

    //Trouble
    UserDto convert(User user);
    User convert(UserDto userDto);

    //USAGES APPLICATION
    UsageApplicationDto convert(UsageApplication usageApplication);

    UsageApplication convert(UsageApplicationDto usageApplicationDto);

    @Mapping(source = "application.id", target = "applicationId")
    UsageApplicationSimpleDto convertToUsageApplicationSimpleDto(UsageApplication usageApplication);

    //CHAINS
    ChaineSimpleDto convertToChaineSimpleDto(Chaine chaine);

    ChaineDto convertToChaineDto(Chaine chaine);

    Chaine convert(ChaineDto chaineDto);

    //DIRECTORIES
    DirectoryDto convert(Directory directory);

    Directory convert(DirectoryDto directoryDto);

    //ENVIRONMENTS
    Environnement convert(EnvironnementCreationDto environnementCreationDto);

    EnvironnementDto convert(Environnement environnement);

    List<EnvironnementDto> convertToEnvironmentDtoList(List<Environnement> environnement);

    Set<EnvironnementDto> convertToEnvironmentDtoSet(Set<Environnement> environnement);

    Environnement convert(EnvironnementDto environnementDto);

    //GLOBALS SETTINGS
    GlobalSettingKeyValueDto convert(GlobalSetting globalSetting);

    List<GlobalSettingKeyValueDto> convertToGlobalSettingKeyValueDtoList(List<GlobalSetting> globalSetting);

    GlobalSetting convert(GlobalSettingKeyValueDto globalSettingKeyValueDto);

    //IDENTITIES
    @Mapping(source = "availableApplication.id", target = "availableApplication")
    CompleteIdentityDto convertToCompleteIdentityDto(Identity identity);

    PublicIdentityDto convertToPublicIdentityDto(Identity identity);

    @Mapping(source = "availableApplication", target = "availableApplication.id")
    Identity convert(CompleteIdentityDto completeIdentityDto);

    @AfterMapping
    default void after(final @MappingTarget Identity identity, final CompleteIdentityDto completeIdentityDto) {
        if (completeIdentityDto.getAvailableApplication() == null) {
            identity.setAvailableApplication(null);
        }
    }

    //LOGS BATCHS
    @Mapping(source = "user.login", target = "login")
    LogBatchDto convert(LogBatch logBatch);

    @Mapping(source = "login", target = "user.login")
    LogBatch convert(LogBatchDto logBatchDto);

    //NORMS
    NormLabelIdDto convertToNormLabelIdDto(Norme norme);

    NormeDto convertToNormeDto(Norme norme);

    Norme convert(NormeDto normeDto);


    List<NormeDiscoverDto> convertToNormeDiscoverDtoList(List<Norme> normeList);

    @Mappings({
            @Mapping(source = "pathShellDirectory", target = "pathShell"),
            @Mapping(source = "fileDate", target = "ctrlMDate")
    })
    Norme convert(NormeDiscoverDto normeDiscoverDto);

    //NOTIFICATIONS
    List<NotificationDto> convertToNotificationDtoList(List<Notification> notificationList);

    //SCHEDULINGS
    SchedulingDto convert(Scheduling scheduling);

    //FILES
    TypeFichierDto convert(TypeFichier typeFichier);

    TypeFichier convert(TypeFichierDto typeFichierDto);

    //K8SJob
    K8SJobDto convert(K8SJob k8SJob);

    K8SJob convert(K8SJobDto k8SJobDto);

}
