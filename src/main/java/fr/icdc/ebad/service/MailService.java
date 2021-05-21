package fr.icdc.ebad.service;


import fr.icdc.ebad.config.properties.EbadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;

@Service
public class MailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender emailSender;
    private final SpringTemplateEngine thymeleafTemplateEngine;
    private final EbadProperties ebadProperties;

    public MailService(ObjectProvider<JavaMailSender> emailSender, SpringTemplateEngine thymeleafTemplateEngine, EbadProperties ebadProperties) {
        this.emailSender = emailSender.getIfAvailable();
        this.thymeleafTemplateEngine = thymeleafTemplateEngine;
        this.ebadProperties = ebadProperties;
    }

    public void sendMailAccreditation(String emails) throws MessagingException {
        if (emailSender == null || !ebadProperties.getEmailNotification().isEnable()) {
            return;
        }
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(new HashMap<>());
        String htmlBody = thymeleafTemplateEngine.process("mail-accreditation.html", thymeleafContext);
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(emails);
        helper.setFrom(ebadProperties.getEmailNotification().getFrom());
        helper.setSubject("EBAD - Demande accréditation en attente");
        helper.setText(htmlBody, true);
        emailSender.send(message);

        LOGGER.debug("mail is sent to " + emails);
    }
}
