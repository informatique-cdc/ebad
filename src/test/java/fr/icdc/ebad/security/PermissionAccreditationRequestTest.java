package fr.icdc.ebad.security;

import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.repository.AccreditationRequestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(PowerMockRunner.class)
@PrepareForTest({SecurityUtils.class,PermissionAccreditationRequest.class})
public class PermissionAccreditationRequestTest {
    @Mock
    private PermissionApplication permissionApplication;

    @Mock
    private AccreditationRequestRepository accreditationRequestRepository;

    @InjectMocks
    private PermissionAccreditationRequest permissionAccreditationRequest;

    @Mock
    private UserDetails userDetails;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void canAcceptAccreditationRequestAdmin() {
        PowerMockito.mockStatic(SecurityUtils.class);
        BDDMockito.given(SecurityUtils.isAdmin()).willReturn(true);
        assertTrue(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void canAcceptAccreditationRequestUserNoAccreditation() {
        PowerMockito.mockStatic(SecurityUtils.class);
        BDDMockito.given(SecurityUtils.isAdmin()).willReturn(false);

        when(accreditationRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());
        assertFalse(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void cantAcceptAccreditationRequestUser() {
        PowerMockito.mockStatic(SecurityUtils.class);
        BDDMockito.given(SecurityUtils.isAdmin()).willReturn(false);

        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(1L)
                .application(Application.builder().id(2L).build())
                .build();
        when(accreditationRequestRepository.findById(eq(1L))).thenReturn(Optional.of(accreditationRequest));
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(false);
        assertFalse(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void canAcceptAccreditationRequestUser() {
        PowerMockito.mockStatic(SecurityUtils.class);
        BDDMockito.given(SecurityUtils.isAdmin()).willReturn(false);

        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(1L)
                .application(Application.builder().id(2L).build())
                .build();
        when(accreditationRequestRepository.findById(eq(1L))).thenReturn(Optional.of(accreditationRequest));
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(true);
        assertTrue(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }
}
