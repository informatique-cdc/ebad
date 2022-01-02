package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.service.EnvironnementService;
import fr.icdc.ebad.service.ShellService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.NewTerminalDto;
import fr.icdc.ebad.web.rest.dto.TerminalCommandDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.sshd.client.channel.ChannelShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/terminals")
@Tag(name = "Terminals", description = "the terminal API")
public class TerminalsResource {
    private final ShellService shellService;
    private final EnvironnementService environnementService;
    private final Map<String, ChannelShell> channelsShell = new HashMap<>();
    private final Map<String, Long> envs = new HashMap<>();
    private final Map<String, String> logins = new HashMap<>();
    private final Map<String, String> sessions = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(TerminalsResource.class);

    public TerminalsResource(ShellService shellService, EnvironnementService environnementService) {
        this.shellService = shellService;
        this.environnementService = environnementService;
    }

    @MessageMapping("/terminal")
    public void sendCommand(TerminalCommandDto terminalCommandDto, Principal principal) throws IOException {
        if(!Objects.equals(logins.get(terminalCommandDto.getId()), principal.getName())){
            LOGGER.info("User {} tried to send command to terminal {}", principal.getName(), terminalCommandDto.getId());
            return;
        }
        LOGGER.debug("User {} send command to terminal {}", principal.getName(), terminalCommandDto.getId());
        OutputStream outputStream = channelsShell.get(terminalCommandDto.getId()).getInvertedIn();
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
            Environnement environment = environnementService.getEnvironnement(envs.get(id));
            ChannelShell channelShell = shellService.startShell(environment,logins.get(id),id);
            channelsShell.put(id,channelShell);
            sessions.put(simpSessionId, id);
        }
    }

    @EventListener
    @Transactional
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws IOException {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");

        if (simpSessionId != null) {
            String id = sessions.get(simpSessionId);
            if(id == null || id.equals("")){
                return;
            }
            LOGGER.debug("Terminal Id is {}", id);
            envs.remove(id);
            logins.remove(id);
            ChannelShell channelShell = channelsShell.get(id);
            channelShell.getSession().close();
            channelShell.getClientSession().close();
            channelShell.close(true);
            channelsShell.remove(id);
            sessions.remove(simpSessionId);
        }
    }

    @GetMapping(path = "/{environmentId}",  produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@permissionEnvironnement.canWrite(#environmentId, principal) && @permissionServiceOpen.canRunTerminal()")
    public NewTerminalDto startTerminal(@PathVariable Long environmentId, Principal principal) {
        String uuid = UUID.randomUUID().toString();
        envs.put(uuid, environmentId);
        logins.put(uuid, principal.getName());
        return NewTerminalDto.builder().id(uuid).build();
    }
}
