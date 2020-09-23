package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.UsageApplicationId;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.service.util.RandomUtil;
import fr.icdc.ebad.web.rest.dto.AuthorityApplicationDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final int NUMBERS_OF_DAY_KEEP_INACTIVATE_USERS = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    public UserService(PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Optional<User> activateRegistration(String key) {
        LOGGER.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key).map(user -> {
            // activate given user for the registration key.
            user.setActivated(true);
            user.setActivationKey(null);
            userRepository.save(user);
            LOGGER.debug("Activated user: {}", user);
            return user;
        });
    }

    @Transactional
    public User createUserInformation(String login, String password, String firstName, String lastName, String email, String langKey) {
        User newUser = new User();
        Set<Authority> authorities = new HashSet<>();

        authorities.add(authorityRepository.getOne("ROLE_USER"));

        newUser.setAuthorities(authorities);

        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(login);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setLangKey(langKey);
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());

        newUser.setCreatedBy(SecurityUtils.getCurrentLogin());
        userRepository.save(newUser);
        LOGGER.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    @Transactional
    public void updateUserInformation(String firstName, String lastName, String email) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentLogin()).ifPresent(u -> {
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setEmail(email);
            userRepository.save(u);
            LOGGER.debug("Changed Information for User: {}", u);
        });
    }

    @Transactional
    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentLogin()).ifPresent(u -> {
            String encryptedPassword = passwordEncoder.encode(password);
            u.setPassword(encryptedPassword);
            userRepository.save(u);
            LOGGER.debug("Changed password for User: {}", u);
        });
    }

    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() throws EbadServiceException {
        return userRepository.findOneByLogin(SecurityUtils.getCurrentLogin()).orElseThrow(() -> new EbadServiceException("L'utilisateur n'existe pas"));
    }


    /**
     * <p>
     * Not activated users should be automatically deleted after 3 days.
     * </p>
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     * </p>
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void removeNotActivatedUsers() {
        DateTime now = new DateTime();
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minusDays(NUMBERS_OF_DAY_KEEP_INACTIVATE_USERS));
        for (User user : users) {
            LOGGER.debug("Deleting not activated user {}", user.getLogin());
            userRepository.delete(user);
        }
    }

    @Transactional
    public User changeAutorisationApplication(AuthorityApplicationDTO authorityDTO) {
        Optional<User> userOptional = userRepository.findOneByLoginUser(authorityDTO.getLoginUser());
        if (!userOptional.isPresent()) {
            return null;
        }
        Application application = new Application();
        application.setId(authorityDTO.getIdApplication());

        User user = userOptional.get();

        UsageApplication usageApplicationToUpdate = null;
        for (UsageApplication usageApplication : userOptional.get().getUsageApplications()) {
            if (usageApplication.getApplication().getId().equals(authorityDTO.getIdApplication())) {
                usageApplicationToUpdate = usageApplication;
                break;
            }
        }

        if (usageApplicationToUpdate == null) {
            UsageApplicationId usageApplicationId = new UsageApplicationId(user.getId(), application.getId());
            usageApplicationToUpdate = UsageApplication.builder().application(application).user(user).usageApplicationId(usageApplicationId).build();
            user.getUsageApplications().add(usageApplicationToUpdate);
        }

        if (authorityDTO.isAddUser()) {
            usageApplicationToUpdate.setCanUse(true);
        }

        if (authorityDTO.isRemoveUser()) {
            usageApplicationToUpdate.setCanUse(false);
        }

        if (authorityDTO.isAddModo()) {
            usageApplicationToUpdate.setCanManage(true);
        }

        if (authorityDTO.isRemoveModo()) {
            usageApplicationToUpdate.setCanManage(false);
        }


        userRepository.save(user);
        if (!usageApplicationToUpdate.isCanManage() && !usageApplicationToUpdate.isCanUse()) {
            user.getUsageApplications().remove(usageApplicationToUpdate);
        }
        return user;

    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Predicate predicate, Pageable pageable) {
        Page<User> users = userRepository.findAll(predicate, pageable);
        for (User user : users.getContent()) {
            user.getAuthorities().size();
            user.getUsageApplications().size();
        }
        return users;
    }

    @Transactional
    public Optional<User> getUser(String login) {
        return userRepository.findOneByLoginUser(login);
    }

    @Transactional
    public Optional<User> inactivateAccount(String login) throws EbadServiceException {
        if (SecurityUtils.getCurrentLogin().equals(login)) {
            throw new EbadServiceException("Impossible de désactiver son propre compte");
        }
        User userSaved = userRepository.findOneByLogin(login)
                .map(user -> {
                    user.setActivationKey(RandomUtil.generateActivationKey());
                    user.setActivated(false);
                    return userRepository.save(user);
                }).orElse(null);

        return Optional.ofNullable(userSaved);
    }

    @Transactional
    public User updateUser(User user) {
        User userTmp = userRepository.getOne(user.getId());
        if (user.getPassword() == null) {
            user.setPassword(userTmp.getPassword());
        } else {
            user.setPassword(getEncodedPassword(user.getPassword()));
        }
        user.setAuthorities(userTmp.getAuthorities());
        user.setUsageApplications(userTmp.getUsageApplications());
        return userRepository.save(user);
    }

    public User createUser(String login, String email, String firstName, String lastName, String password) throws EbadServiceException {
        Optional<User> userLogin = userRepository.findOneByLogin(login);
        if (userLogin.isPresent()) {
            throw new EbadServiceException("User already exists with this login");
        }

        Optional<User> userEmail = userRepository.findOneByEmail(email);
        if (userEmail.isPresent()) {
            throw new EbadServiceException("Email address already in user");
        }

        return createUserInformation(login, password, firstName, lastName, email.toLowerCase(), "fr_FR");
    }

    public User updateUser(Long id, String login, String email, String firstName, String lastName, String password) throws EbadServiceException {
        Optional<User> optionalUserToUpdate = userRepository.findOneById(id);
        User userToUpdate = optionalUserToUpdate.orElseThrow(() -> new EbadServiceException("User does not exist"));
        if (password != null) {
            userToUpdate.setPassword(getEncodedPassword(password));
        }
        userToUpdate.setLogin(login);
        userToUpdate.setEmail(email);
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);

        return userRepository.save(userToUpdate);
    }

    public User changeRoles(String loginUser, boolean roleAdmin, boolean roleUser) throws EbadServiceException {
        Optional<User> userOptional = userRepository.findOneByLoginUser(loginUser);
        User user = userOptional.orElseThrow(() -> new EbadServiceException("User does not exist"));

        Authority authorityUser = new Authority();
        authorityUser.setName("ROLE_USER");

        Authority authorityAdmin = new Authority();
        authorityAdmin.setName("ROLE_ADMIN");

        if (roleUser && !user.getAuthorities().contains(authorityUser)) {
            user.getAuthorities().add(authorityUser);
        } else if (!roleUser && user.getAuthorities().contains(authorityUser)) {
            user.getAuthorities().remove(authorityUser);
        }

        if (roleAdmin && !user.getAuthorities().contains(authorityAdmin)) {
            user.getAuthorities().add(authorityAdmin);
        } else if (!roleAdmin && user.getAuthorities().contains(authorityAdmin)) {
            user.getAuthorities().remove(authorityAdmin);
        }

        return userRepository.save(user);
    }
}
