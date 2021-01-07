package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.QAccreditationRequest;
import fr.icdc.ebad.domain.StateRequest;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AccreditationRequestRepository;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.AccreditationRequestDto;
import fr.icdc.ebad.web.rest.dto.AuthorityApplicationDTO;
import ma.glasnost.orika.MapperFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AccreditationRequestService {
    private final AccreditationRequestRepository accreditationRequestRepository;
    private final UserService userService;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapperFacade mapperFacade;

    public AccreditationRequestService(AccreditationRequestRepository accreditationRequestRepository, UserService userService, ApplicationRepository applicationRepository, NotificationService notificationService, SimpMessagingTemplate messagingTemplate, MapperFacade mapperFacade) {
        this.accreditationRequestRepository = accreditationRequestRepository;
        this.userService = userService;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
        this.mapperFacade = mapperFacade;
    }

    @Transactional
    public AccreditationRequest requestNewAccreditation(Long applicationId, boolean isWantManage, boolean isWantUse) throws EbadServiceException {
        Application application = applicationRepository.findById(applicationId).orElseThrow(EbadServiceException::new);
        User user = userService.getUser(SecurityUtils.getCurrentLogin()).orElseThrow(EbadServiceException::new);
        AccreditationRequest accreditationRequest = AccreditationRequest.builder()
                .user(user)
                .application(application)
                .state(StateRequest.SENT)
                .wantManage(isWantManage)
                .wantUse(isWantUse)
                .build();

        AccreditationRequest result = accreditationRequestRepository.save(accreditationRequest);

        AccreditationRequestDto[] sendNotif = {mapperFacade.map(result, AccreditationRequestDto.class)};
        application.getUsageApplications()
                .parallelStream()
                .filter(UsageApplication::isCanManage)
                .forEach(usageApplication -> {
                    notificationService.createNotification("Une nouvelle demande d'accréditation vient d'être soumise", usageApplication.getUser());
                    messagingTemplate.convertAndSendToUser(usageApplication.getUser().getLogin(), "/queue/accreditations", sendNotif);
                });

        return result;
    }

    @Transactional(readOnly = true)
    public Page<AccreditationRequest> getAllAccreditationRequestToAnswer(Pageable pageable) {
        if (SecurityUtils.isAdmin()) {
            Predicate predicate = QAccreditationRequest.accreditationRequest.state.eq(StateRequest.SENT);
            return accreditationRequestRepository.findAll(predicate, pageable);
        }

        PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        Page<Application> applications = applicationRepository.findAllManagedByUser(SecurityUtils.getCurrentLogin(), pageRequest);

        Predicate predicate = QAccreditationRequest
                .accreditationRequest
                .application
                .in(applications.getContent())
                .and(QAccreditationRequest.accreditationRequest.state.eq(StateRequest.SENT));

        return accreditationRequestRepository.findAll(predicate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AccreditationRequest> getMyAccreditationRequest(Pageable pageable) {
        Predicate predicate = QAccreditationRequest.accreditationRequest.user.login.eq(SecurityUtils.getCurrentLogin());
        return accreditationRequestRepository.findAll(predicate, pageable);
    }

    @Transactional
    public void answerToRequest(Long idAccreditationRequest, boolean isAccepted) throws EbadServiceException {
        AccreditationRequest accreditationRequest = accreditationRequestRepository.findByIdAndState(idAccreditationRequest, StateRequest.SENT).orElseThrow(EbadServiceException::new);
        if (!isAccepted) {
            accreditationRequest.setState(StateRequest.REJECTED);
            accreditationRequestRepository.save(accreditationRequest);
        } else {

            AuthorityApplicationDTO authorityApplicationDTO = new AuthorityApplicationDTO();
            authorityApplicationDTO.setLoginUser(accreditationRequest.getUser().getLogin());
            authorityApplicationDTO.setIdApplication(accreditationRequest.getApplication().getId());
            authorityApplicationDTO.setAddModo(accreditationRequest.isWantManage());
            authorityApplicationDTO.setAddUser(accreditationRequest.isWantUse());
            if (null != userService.changeAutorisationApplication(authorityApplicationDTO)) {
                accreditationRequest.setState(StateRequest.ACCEPTED);
                accreditationRequestRepository.save(accreditationRequest);
            }
        }

        AccreditationRequestDto[] accreditationRequestDtos = {mapperFacade.map(accreditationRequest, AccreditationRequestDto.class)};
        accreditationRequest.getApplication().getUsageApplications()
                .parallelStream()
                .filter(UsageApplication::isCanManage)
                .forEach(usageApplication ->
                        messagingTemplate.convertAndSendToUser(usageApplication.getUser().getLogin(), "/queue/accreditationsResponses", accreditationRequestDtos)
                );
    }

}
