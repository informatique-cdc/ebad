package fr.icdc.ebad.service;


import fr.icdc.ebad.config.properties.EbadProperties;
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
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine thymeleafTemplateEngine;
    private final EbadProperties ebadProperties;

    public MailService(JavaMailSender emailSender, SpringTemplateEngine thymeleafTemplateEngine, EbadProperties ebadProperties) {
        this.emailSender = emailSender;
        this.thymeleafTemplateEngine = thymeleafTemplateEngine;
        this.ebadProperties = ebadProperties;
    }

    public void sendMailAccreditation(String emails) throws MessagingException {
        // TODO DTROUILLET user ConditionalOnProperty spring annotation
        if(!ebadProperties.getEmailNotification().isEnable()){
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
    }
}
