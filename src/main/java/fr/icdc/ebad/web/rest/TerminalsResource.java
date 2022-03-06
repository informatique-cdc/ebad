package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Terminal;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.TerminalRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.service.EnvironnementService;
import fr.icdc.ebad.service.ShellService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.NewTerminalDto;
import fr.icdc.ebad.web.rest.dto.TerminalCommandDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/terminals")
@Tag(name = "Terminals", description = "the terminal API")
public class TerminalsResource {
    private final ShellService shellService;
    private final EnvironnementService environnementService;
    private final UserRepository userRepository; //FIXME DTROUILLET REMOVE THIS REPO
    private final TerminalRepository terminalRepository; //FIXME DTROUILLET REMOVE THIS REPO
    private static final Logger LOGGER = LoggerFactory.getLogger(TerminalsResource.class);

    public TerminalsResource(ShellService shellService, EnvironnementService environnementService, UserRepository userRepository, TerminalRepository terminalRepository) {
        this.shellService = shellService;
        this.environnementService = environnementService;
        this.userRepository = userRepository;
        this.terminalRepository = terminalRepository;
    }

    @MessageMapping("/terminal")
    @Transactional
    public void sendCommand(TerminalCommandDto terminalCommandDto, Principal principal) throws IOException {
        Optional<Terminal> terminal = terminalRepository.findById(UUID.fromString(terminalCommandDto.getId()));
        if(terminal.isEmpty())
            return;

        if(!Objects.equals(terminal.get().getUser().getLogin(), principal.getName())){
            LOGGER.info("User {} tried to send command to terminal {}", principal.getName(), terminalCommandDto.getId());
            return;
        }

        LOGGER.debug("User {} send command to terminal {}", principal.getName(), terminalCommandDto.getId());
        OutputStream outputStream = shellService.getLocalChannelShell(terminalCommandDto.getId()).getInvertedIn();
        outputStream.write(terminalCommandDto.getKey().getBytes());
        outputStream.flush();
    }

    @EventListener
    @Transactional
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) throws IOException, EbadServiceException {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpDestination = (String) message.getHeaders().get("simpDestination");
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");

        if (simpDestination != null && simpDestination.contains("terminal-")) {
            String id = simpDestination.replace("/user/queue/terminal-", "");
            if(id.equals("")){
                return;
            }
            LOGGER.debug("Terminal Id is {}", id);
            Terminal result = terminalRepository.getById(UUID.fromString(id));
            result.setSessionId(simpSessionId);
            terminalRepository.save(result);
            terminalRepository.flush();
            shellService.startShell(id);
        }
    }

    @EventListener
    @Transactional
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws IOException {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");

        if (simpSessionId != null) {
            LOGGER.debug("Terminal simpSessionId is {}", simpSessionId);
            for(Terminal terminal : terminalRepository.findAllBySessionId(simpSessionId)) {
                shellService.deleteLocalChannelShell(terminal.getId().toString());
            }
        }
    }

    @GetMapping(path = "/{environmentId}",  produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@permissionEnvironnement.canWrite(#environmentId, principal) && @permissionServiceOpen.canRunTerminal()")
    @Transactional
    public NewTerminalDto startTerminal(@PathVariable Long environmentId, Principal principal) {
        Terminal terminal = new Terminal();
        terminal.setEnvironment(environnementService.getEnvironnement(environmentId));
        Optional<User> userOptional = userRepository.findOneByLogin(principal.getName());
        terminal.setUser(userOptional.orElseThrow());
        Terminal result = terminalRepository.save(terminal);
        return NewTerminalDto.builder().id(result.getId().toString()).build();
    }
}
