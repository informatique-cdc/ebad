package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface TerminalRepository extends JpaRepository<Terminal, UUID> {
    public List<Terminal> findAllBySessionId(String sessionId);
}
