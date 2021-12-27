package fr.icdc.ebad.web.rest.mapstruct;

import fr.icdc.ebad.domain.*;
import fr.icdc.ebad.web.rest.dto.*;
import org.mapstruct.*;

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
    LogBatchDto convert(LogBatch logBatch);

    LogBatch convert(LogBatchDto logBatchDto);

    //NORMS
    NormLabelIdDto convertToNormLabelIdDto(Norme norme);

    NormeDto convertToNormeDto(Norme norme);

    Norme convert(NormeDto normeDto);

    //NOTIFICATIONS
    List<NotificationDto> convertToNotificationDtoList(List<Notification> notificationList);

    //SCHEDULINGS
    SchedulingDto convert(Scheduling scheduling);

    //FILES
    TypeFichierDto convert(TypeFichier typeFichier);

    TypeFichier convert(TypeFichierDto typeFichierDto);
}