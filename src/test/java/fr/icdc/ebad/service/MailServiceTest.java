package fr.icdc.ebad.service;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Notification;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MailServiceTest {
    @InjectMocks
    private MailService mailService;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SpringTemplateEngine thymeleafTemplateEngine;

    @Spy
    private EbadProperties ebadProperties;

    @Spy
    private MimeMessage mimeMessage = new MimeMessage((Session) null);


    @Test
    public void testMailDisable() throws MessagingException {
        ebadProperties.getEmailNotification().setEnable(false);
        mailService.sendMailAccreditation("test@test.com");
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    public void testMail() throws MessagingException, IOException {
        ebadProperties.getEmailNotification().setEnable(true);
        ebadProperties.getEmailNotification().setFrom("ebad@localhost");

        when(thymeleafTemplateEngine.process(eq("mail-accreditation.html"), any(Context.class))).thenReturn("TEST");
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        mailService.sendMailAccreditation("test@test.com");

        verify(javaMailSender).send(eq(mimeMessage));
        assertEquals("test@test.com", mimeMessage.getAllRecipients()[0].toString());
        assertEquals("ebad@localhost", mimeMessage.getFrom()[0].toString());
        assertEquals("EBAD - Demande accréditation en attente", mimeMessage.getSubject());
    }

}