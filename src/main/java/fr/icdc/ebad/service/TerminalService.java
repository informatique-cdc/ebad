package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Terminal;
import fr.icdc.ebad.repository.TerminalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TerminalService {
    private final TerminalRepository terminalRepository;

    public TerminalService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    @Transactional
    public Terminal save(Terminal terminal){
        return terminalRepository.save(terminal);
    }

    @Transactional(readOnly = true)
    public Optional<Terminal> findById(UUID id){
        return terminalRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Terminal> findBySessionId(String sessionId){
        return terminalRepository.findAllBySessionId(sessionId);
    }
}
