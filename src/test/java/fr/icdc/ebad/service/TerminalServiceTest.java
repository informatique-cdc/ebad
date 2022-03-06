package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Terminal;
import fr.icdc.ebad.repository.TerminalRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TerminalServiceTest {
    @Mock
    private TerminalRepository terminalRepository;

    @InjectMocks
    private TerminalService terminalService;

    @Test
    public void save() {
        Terminal terminal = Terminal.builder().build();
        when(terminalRepository.save(terminal)).thenReturn(terminal);
        assertEquals(terminal, terminalService.save(terminal));
        verify(terminalRepository).save(terminal);
    }

    @Test
    public void findById() {
        UUID id = UUID.randomUUID();
        Terminal terminal = Terminal.builder().id(id).build();
        when(terminalRepository.findById(id)).thenReturn(Optional.of(terminal));
        assertEquals(terminal, terminalService.findById(id).get());
    }

    @Test
    public void findBySessionId() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Terminal terminal1 = Terminal.builder().id(id1).build();
        Terminal terminal2 = Terminal.builder().id(id2).build();

        when(terminalRepository.findAllBySessionId("session")).thenReturn(List.of(terminal1, terminal2));

        List<Terminal> terminals = terminalService.findBySessionId("session");
        assertEquals(2, terminals.size());
        assertTrue(terminals.contains(terminal1));
        assertTrue(terminals.contains(terminal2));

    }
}
