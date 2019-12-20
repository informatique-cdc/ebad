package fr.icdc.ebad.service;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.AuthorityApplicationDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthorityRepository authorityRepository;

    @Test
    public void activateRegistration() {
        User user = User.builder().activated(false).activationKey("test").id(1L).build();
        when(userRepository.findOneByActivationKey(eq("test"))).thenReturn(Optional.of(user));

        Optional<User> result = userService.activateRegistration("test");

        verify(userRepository).save(eq(user));

        assertTrue(result.isPresent());
        assertTrue(result.get().isActivated());
        assertNull(result.get().getActivationKey());
    }

    @Test
    public void createUserInformation() {
        Authority authorityUser = new Authority("ROLE_USER", new HashSet<>());
        when(passwordEncoder.encode(eq("password_decode"))).thenReturn("password_encode");
        when(authorityRepository.getOne(eq("ROLE_USER"))).thenReturn(authorityUser);

        userService.createUserInformation("dtrouillet", "password_decode", "Damien", "Trouillet", "damien.trouillet@test.fr", "fr_FR");

        verify(userRepository).save(argThat(user -> {
            return !user.isActivated()
                    && user.getAuthorities().contains(authorityUser)
                    && user.getPassword().equals("password_encode")
                    && user.getEmail().equals("damien.trouillet@test.fr")
                    && user.getFirstName().equals("Damien")
                    && user.getLastName().equals("Trouillet");
        }));
    }

    @Test
    @WithMockUser("dtrouillet")
    public void updateUserInformation() {
        User user = User.builder().activated(true).id(1L).build();
        when(userRepository.findOneByLogin(eq("dtrouillet"))).thenReturn(Optional.of(user));

        userService.updateUserInformation("Damien", "Trouillet", "damien.trouillet@test.fr");

        verify(userRepository).save(argThat(userModify -> {
            return user.isActivated()
                    && user.getEmail().equals("damien.trouillet@test.fr")
                    && user.getFirstName().equals("Damien")
                    && user.getLastName().equals("Trouillet");
        }));
    }

    @Test
    @WithMockUser("dtrouillet")
    public void changePassword() {
        User user = User.builder().activated(true).id(1L).build();
        when(userRepository.findOneByLogin(eq("dtrouillet"))).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(eq("password_decode"))).thenReturn("password_encode");

        userService.changePassword("password_decode");

        verify(userRepository).save(argThat(userModify ->
                user.getPassword().equals("password_encode")
        ));

    }

    @Test
    public void getEncodedPassword() {
        when(passwordEncoder.encode(eq("password_decode"))).thenReturn("password_encode");
        String result = userService.getEncodedPassword("password_decode");
        assertEquals("password_encode", result);
    }

    @Test
    @WithMockUser("dtrouillet")
    public void getUserWithAuthorities() throws EbadServiceException {
        User user1 = User.builder().activated(false).id(1L).build();
        user1.setAuthorities(new HashSet<>());
        user1.getAuthorities().add(new Authority("ROLE_USER", new HashSet<>()));
        when(userRepository.findOneByLogin(eq("dtrouillet"))).thenReturn(Optional.of(user1));

        User result = userService.getUserWithAuthorities();

        assertEquals(user1, result);
    }

    @Test
    public void removeNotActivatedUsers() {
        User user1 = User.builder().activated(false).id(1L).build();
        User user2 = User.builder().activated(false).id(2L).build();

        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        when(userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(any())).thenReturn(userList);
        userService.removeNotActivatedUsers();

        verify(userRepository).delete(eq(user1));
        verify(userRepository).delete(eq(user2));
    }

    @Test
    public void changeAutorisationApplication() {
        UsageApplication usageApplication = new UsageApplication();
        usageApplication.setCanManage(false);
        usageApplication.setCanUse(true);
        usageApplication.setApplication(Application.builder().id(1L).build());
        Set<UsageApplication> usageApplicationSet = new HashSet<>();
        usageApplicationSet.add(usageApplication);


        User user1 = User.builder().activated(false).id(1L).build();
        user1.setUsageApplications(usageApplicationSet);

        when(userRepository.findOneByLoginUser(eq("dtrouillet"))).thenReturn(Optional.of(user1));

        AuthorityApplicationDTO authorityApplicationDTO = new AuthorityApplicationDTO();
        authorityApplicationDTO.setAddModo(true);
        authorityApplicationDTO.setIdApplication(1L);
        authorityApplicationDTO.setLoginUser("dtrouillet");

        User result = userService.changeAutorisationApplication(authorityApplicationDTO);

        verify(userRepository).save(argThat(user -> {
            UsageApplication usageApplicationRes = (new ArrayList<>(user.getUsageApplications())).get(0);
            return usageApplicationRes.isCanManage()
                    && usageApplicationRes.isCanUse()
                    && user.getId().equals(1L);
        }));


        UsageApplication resultUsageApplication = (new ArrayList<>(result.getUsageApplications())).get(0);
        assertTrue(resultUsageApplication.isCanManage());
        assertTrue(resultUsageApplication.isCanUse());
        assertEquals(1L, user1.getId(), 0);

    }

    @Test
    public void changeAutorisationApplication2() {
        UsageApplication usageApplication = new UsageApplication();
        usageApplication.setCanManage(false);
        usageApplication.setCanUse(true);
        usageApplication.setApplication(Application.builder().id(1L).build());
        Set<UsageApplication> usageApplicationSet = new HashSet<>();
        usageApplicationSet.add(usageApplication);


        User user1 = User.builder().activated(false).id(1L).build();
        user1.setUsageApplications(usageApplicationSet);

        when(userRepository.findOneByLoginUser(eq("dtrouillet"))).thenReturn(Optional.of(user1));

        AuthorityApplicationDTO authorityApplicationDTO = new AuthorityApplicationDTO();
        authorityApplicationDTO.setIdApplication(1L);
        authorityApplicationDTO.setLoginUser("dtrouillet");

        authorityApplicationDTO.setRemoveUser(true);
        User result = userService.changeAutorisationApplication(authorityApplicationDTO);

        verify(userRepository).save(argThat(user -> {
            UsageApplication usageApplicationRes = (new ArrayList<>(user.getUsageApplications())).get(0);
            return !usageApplicationRes.isCanManage()
                    && !usageApplicationRes.isCanUse()
                    && user.getId().equals(1L);
        }));


        UsageApplication resultUsageApplication = (new ArrayList<>(result.getUsageApplications())).get(0);
        assertFalse(resultUsageApplication.isCanManage());
        assertFalse(resultUsageApplication.isCanUse());
        assertEquals(1L, user1.getId(), 0);

    }

    @Test
    public void changeAutorisationApplication3() {
        UsageApplication usageApplication = new UsageApplication();
        usageApplication.setCanManage(false);
        usageApplication.setCanUse(true);
        usageApplication.setApplication(Application.builder().id(1L).build());
        Set<UsageApplication> usageApplicationSet = new HashSet<>();
        usageApplicationSet.add(usageApplication);


        User user1 = User.builder().activated(false).id(1L).build();
        user1.setUsageApplications(usageApplicationSet);

        when(userRepository.findOneByLoginUser(eq("dtrouillet"))).thenReturn(Optional.of(user1));

        AuthorityApplicationDTO authorityApplicationDTO = new AuthorityApplicationDTO();
        authorityApplicationDTO.setIdApplication(1L);
        authorityApplicationDTO.setLoginUser("dtrouillet");

        authorityApplicationDTO.setAddModo(false);
        authorityApplicationDTO.setRemoveUser(false);
        authorityApplicationDTO.setRemoveModo(true);
        authorityApplicationDTO.setAddUser(true);
        User result = userService.changeAutorisationApplication(authorityApplicationDTO);

        verify(userRepository).save(argThat(user -> {
            UsageApplication usageApplicationRes = (new ArrayList<>(user.getUsageApplications())).get(0);
            return !usageApplicationRes.isCanManage()
                    && usageApplicationRes.isCanUse()
                    && user.getId().equals(1L);
        }));


        UsageApplication resultUsageApplication = (new ArrayList<>(result.getUsageApplications())).get(0);
        assertFalse(resultUsageApplication.isCanManage());
        assertTrue(resultUsageApplication.isCanUse());
        assertEquals(1L, user1.getId(), 0);
    }


    @Test
    public void changeAutorisationApplication4() {
        when(userRepository.findOneByLoginUser(eq("dtrouillet"))).thenReturn(Optional.empty());

        AuthorityApplicationDTO authorityApplicationDTO = new AuthorityApplicationDTO();
        authorityApplicationDTO.setIdApplication(1L);
        authorityApplicationDTO.setLoginUser("dtrouillet");

        authorityApplicationDTO.setAddModo(false);
        authorityApplicationDTO.setRemoveUser(false);
        authorityApplicationDTO.setRemoveModo(true);
        authorityApplicationDTO.setAddUser(true);
        User result = userService.changeAutorisationApplication(authorityApplicationDTO);

        assertNull(result);
    }

    @Test
    public void changeAutorisationApplication5() {
        Set<UsageApplication> usageApplicationSet = new HashSet<>();


        User user1 = User.builder().activated(false).id(1L).build();
        user1.setUsageApplications(usageApplicationSet);

        when(userRepository.findOneByLoginUser(eq("dtrouillet"))).thenReturn(Optional.of(user1));

        AuthorityApplicationDTO authorityApplicationDTO = new AuthorityApplicationDTO();
        authorityApplicationDTO.setIdApplication(1L);
        authorityApplicationDTO.setLoginUser("dtrouillet");

        authorityApplicationDTO.setAddModo(false);
        authorityApplicationDTO.setRemoveUser(false);
        authorityApplicationDTO.setRemoveModo(true);
        authorityApplicationDTO.setAddUser(true);
        User result = userService.changeAutorisationApplication(authorityApplicationDTO);

        verify(userRepository).save(argThat(user -> {
            UsageApplication usageApplicationRes = (new ArrayList<>(user.getUsageApplications())).get(0);
            return !usageApplicationRes.isCanManage()
                    && usageApplicationRes.isCanUse()
                    && user.getId().equals(1L);
        }));


        UsageApplication resultUsageApplication = (new ArrayList<>(result.getUsageApplications())).get(0);
        assertFalse(resultUsageApplication.isCanManage());
        assertTrue(resultUsageApplication.isCanUse());
        assertEquals(1L, user1.getId(), 0);
    }


    @Test
    public void getAllUsers() {
        List<User> users = new ArrayList<>();
        User user1 = User.builder().activated(true).id(1L).build();
        User user2 = User.builder().activated(true).id(2L).build();

        users.add(user1);
        users.add(user2);

        when(userRepository.findAll(any(Sort.class))).thenReturn(users);

        List<User> results = userService.getAllUsers();

        assertEquals(2, results.size());
        assertTrue(results.contains(user1));
        assertTrue(results.contains(user2));
    }

    @Test
    public void getUser() {
        User user1 = User.builder().activated(true).id(1L).login("dtrouillet").build();
        when(userRepository.findOneByLoginUser(eq("dtrouillet"))).thenReturn(Optional.of(user1));

        Optional<User> result = userService.getUser("dtrouillet");

        assertTrue(result.isPresent());
        assertEquals(user1, result.get());
    }

    @Test
    public void inactivateAccount() throws EbadServiceException {
        User user1 = User.builder().activated(true).id(1L).login("dtrouillet").build();
        when(userRepository.findOneByLogin(eq("dtrouillet"))).thenReturn(Optional.of(user1));
        when(userRepository.save(eq(user1))).thenReturn(user1);

        Optional<User> result = userService.inactivateAccount("dtrouillet");

        verify(userRepository).save(argThat(user ->
                !user.isActivated() && !user.getActivationKey().isEmpty() && user.getLogin().equals("dtrouillet")
        ));
        assertTrue(result.isPresent());
        assertFalse(result.get().isActivated());
        assertFalse(result.get().getActivationKey().isEmpty());
    }

    @Test
    public void updateUser() {
        User user1 = User.builder().activated(true).id(1L).login("dtrouillet").password("test").build();
        when(userRepository.getOne(eq(1L))).thenReturn(user1);
        User userUpdated = User.builder().activated(true).id(1L).login("toto").build();
        when(userRepository.save(eq(userUpdated))).thenReturn(userUpdated);

        User result = userService.updateUser(userUpdated);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("test") && user.getLogin().equals("toto")
        ));

        assertEquals("toto", result.getLogin());
        assertEquals("test", result.getPassword());
    }

    @Test
    public void updateUserWithPassword() {
        User user1 = User.builder().activated(true).id(1L).login("dtrouillet").password("test").build();
        when(userRepository.getOne(eq(1L))).thenReturn(user1);
        when(passwordEncoder.encode(eq("password_decode"))).thenReturn("password_encode");

        User userUpdated = User.builder().activated(true).id(1L).login("toto").password("password_decode").build();
        when(userRepository.save(eq(userUpdated))).thenReturn(userUpdated);

        User result = userService.updateUser(userUpdated);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("password_encode") && user.getLogin().equals("toto")
        ));

        assertEquals("toto", result.getLogin());
        assertEquals("password_encode", result.getPassword());
    }
}
