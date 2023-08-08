package fr.icdc.ebad.service;

import fr.icdc.ebad.config.properties.EbadProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MailServiceTest {
    @InjectMocks
    private MailService mailService;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private ObjectProvider<JavaMailSender> objectProviderJavaMailSender;

    @Mock
    private SpringTemplateEngine thymeleafTemplateEngine;

    @Spy
    private EbadProperties ebadProperties;

    @Spy
    private MimeMessage mimeMessage = new MimeMessage((Session) null);

    @Before
    public void init(){
        doReturn(javaMailSender).when(objectProviderJavaMailSender).getIfAvailable();
    }

    @Test
    public void testMailDisable() throws MessagingException, MailException {
        ebadProperties.getEmailNotification().setEnable(false);
        mailService.sendMailAccreditation("test@test.com");
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    public void testMail() throws MessagingException {
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
