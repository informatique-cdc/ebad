package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.StateRequest;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AccreditationRequestRepository;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccreditationRequestServiceTest {
    @InjectMocks
    private AccreditationRequestService accreditationRequestService;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserService userService;

    @Mock
    private AccreditationRequestRepository accreditationRequestRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    NotificationService notificationService;

    @Test(expected = EbadServiceException.class)
    public void requestNewAccreditationNoApplication() throws EbadServiceException {
        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.empty());
        accreditationRequestService.requestNewAccreditation(1L, true, false);
    }

    @Test(expected = EbadServiceException.class)
    public void requestNewAccreditationNoUser() throws EbadServiceException {
        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(Application.builder().build()));
        accreditationRequestService.requestNewAccreditation(1L, true, false);
    }

    @Test
    public void requestNewAccreditation() throws EbadServiceException {
        AccreditationRequest accreditationRequest = AccreditationRequest.builder()
                .user(User.builder().login("testlogin").build())
                .application(
                        Application.builder().id(1L).usageApplications(
                                Set.of(UsageApplication.builder().canManage(true).user(User.builder().build()).build())
                        ).build())
                .state(StateRequest.SENT)
                .wantManage(true)
                .wantUse(false)
                .build();

        AccreditationRequest accreditationRequestWithId = AccreditationRequest.builder()
                .id(99L)
                .user(User.builder().login("testlogin").build())
                .application(Application.builder().id(1L).build())
                .state(StateRequest.SENT)
                .wantManage(true)
                .wantUse(false)
                .build();

        when(applicationRepository.findById(eq(1L))).thenReturn(Optional.of(accreditationRequest.getApplication()));
        when(userService.getUser(any())).thenReturn(Optional.of(User.builder().login("testlogin").build()));

        when(accreditationRequestRepository.save(eq(accreditationRequest))).thenReturn(accreditationRequestWithId);
        doNothing().when(notificationService).createNotification(any(), any());
        AccreditationRequest result = accreditationRequestService.requestNewAccreditation(1L, true, false);

        verify(accreditationRequestRepository).save(eq(accreditationRequest));
        assertEquals(accreditationRequestWithId, result);

    }

    @Test
    public void getAllAccreditationRequestToAnswerAdmin() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<AccreditationRequest> accreditationRequestList = new ArrayList<>();
        AccreditationRequest accreditationRequest1 = AccreditationRequest
                .builder()
                .id(1L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(true)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        AccreditationRequest accreditationRequest2 = AccreditationRequest
                .builder()
                .id(4L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(false)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        accreditationRequestList.add(accreditationRequest1);
        accreditationRequestList.add(accreditationRequest2);
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(accreditationRequestRepository.findAll(any(Predicate.class), eq(pageRequest))).thenReturn(new PageImpl<>(accreditationRequestList));

        Page<AccreditationRequest> result = accreditationRequestService.getAllAccreditationRequestToAnswer(pageRequest);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(accreditationRequest1));
        assertTrue(result.getContent().contains(accreditationRequest2));
    }

    @Test
    public void getAllAccreditationRequestToAnswer() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        PageImpl<Application> applicationPage = new PageImpl<>(applications);
        when(applicationRepository.findAllManagedByUser(any(), any())).thenReturn(applicationPage);


        List<AccreditationRequest> accreditationRequestList = new ArrayList<>();
        AccreditationRequest accreditationRequest1 = AccreditationRequest
                .builder()
                .id(1L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(true)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        AccreditationRequest accreditationRequest2 = AccreditationRequest
                .builder()
                .id(4L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(false)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        accreditationRequestList.add(accreditationRequest1);
        accreditationRequestList.add(accreditationRequest2);
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(accreditationRequestRepository.findAll(any(Predicate.class), eq(pageRequest))).thenReturn(new PageImpl<>(accreditationRequestList));

        Page<AccreditationRequest> result = accreditationRequestService.getAllAccreditationRequestToAnswer(pageRequest);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(accreditationRequest1));
        assertTrue(result.getContent().contains(accreditationRequest2));
    }

    @Test
    public void getMyAccreditationRequest() {
        when(authentication.getPrincipal()).thenReturn("testlogin");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<AccreditationRequest> accreditationRequestList = new ArrayList<>();
        AccreditationRequest accreditationRequest1 = AccreditationRequest
                .builder()
                .id(1L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(true)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        AccreditationRequest accreditationRequest2 = AccreditationRequest
                .builder()
                .id(4L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(false)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        accreditationRequestList.add(accreditationRequest1);
        accreditationRequestList.add(accreditationRequest2);
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(accreditationRequestRepository.findAll(any(Predicate.class), eq(pageRequest))).thenReturn(new PageImpl<>(accreditationRequestList));

        Page<AccreditationRequest> result = accreditationRequestService.getMyAccreditationRequest(pageRequest);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(accreditationRequest1));
        assertTrue(result.getContent().contains(accreditationRequest2));
    }

    @Test(expected = EbadServiceException.class)
    public void answerToRequestNoRequest() throws EbadServiceException {
        when(accreditationRequestRepository.findByIdAndState(eq(1L), eq(StateRequest.SENT))).thenReturn(Optional.empty());
        accreditationRequestService.answerToRequest(1L, true);
    }

    @Test
    public void answerToRequestReject() throws EbadServiceException {
        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(1L)
                .state(StateRequest.SENT)
                .application(Application.builder().id(2L).build())
                .user(User.builder().login("test").build())
                .wantManage(true)
                .build();

        AccreditationRequest accreditationRequestRejected = AccreditationRequest
                .builder()
                .id(1L)
                .state(StateRequest.REJECTED)
                .application(Application.builder().id(2L).build())
                .user(User.builder().login("test").build())
                .wantManage(true)
                .build();
        when(accreditationRequestRepository.findByIdAndState(eq(1L), eq(StateRequest.SENT))).thenReturn(Optional.of(accreditationRequest));
        accreditationRequestService.answerToRequest(1L, false);

        verify(accreditationRequestRepository).save(eq(accreditationRequestRejected));
    }

    @Test
    public void answerToRequestAccept() throws EbadServiceException {
        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(1L)
                .state(StateRequest.SENT)
                .application(Application.builder().id(2L).build())
                .user(User.builder().login("testlogin").build())
                .wantManage(true)
                .build();

        AccreditationRequest accreditationRequestAccepted = AccreditationRequest
                .builder()
                .id(1L)
                .state(StateRequest.ACCEPTED)
                .application(Application.builder().id(2L).build())
                .user(User.builder().login("testlogin").build())
                .wantManage(true)
                .build();
        when(accreditationRequestRepository.findByIdAndState(eq(1L), eq(StateRequest.SENT))).thenReturn(Optional.of(accreditationRequest));
        when(userService.changeAutorisationApplication(argThat(authorityApplicationDTO ->
                "testlogin".equals(authorityApplicationDTO.getLoginUser())
                        && 2L == authorityApplicationDTO.getIdApplication()
                        && authorityApplicationDTO.isAddModo()
                        && !authorityApplicationDTO.isAddUser()
                        && !authorityApplicationDTO.isRemoveModo()
                        && !authorityApplicationDTO.isRemoveUser()
        ))).thenReturn(User.builder().login("testlogin").build());

        accreditationRequestService.answerToRequest(1L, true);

        verify(accreditationRequestRepository).save(eq(accreditationRequestAccepted));

        verify(userService).changeAutorisationApplication(argThat(authorityApplicationDTO ->
                "testlogin".equals(authorityApplicationDTO.getLoginUser())
                        && 2L == authorityApplicationDTO.getIdApplication()
                        && authorityApplicationDTO.isAddModo()
                        && !authorityApplicationDTO.isAddUser()
                        && !authorityApplicationDTO.isRemoveModo()
                        && !authorityApplicationDTO.isRemoveUser()
        ));
    }

    @Test
    public void answerToRequestAcceptNoUser() throws EbadServiceException {
        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(1L)
                .state(StateRequest.SENT)
                .application(Application.builder().id(2L).build())
                .user(User.builder().login("testlogin").build())
                .wantManage(true)
                .build();

        when(accreditationRequestRepository.findByIdAndState(eq(1L), eq(StateRequest.SENT))).thenReturn(Optional.of(accreditationRequest));

        accreditationRequestService.answerToRequest(1L, true);

        verify(accreditationRequestRepository, never()).save(any());

    }
}
