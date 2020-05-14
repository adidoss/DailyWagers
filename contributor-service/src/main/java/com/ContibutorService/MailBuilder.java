package com.ContibutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.ContibutorService.models.Beneficiary;

@Service
public class MailBuilder {
	
	@Autowired
	private JavaMailSender mailSender;
	
	private TemplateEngine templateEngine;
	 
    @Autowired
    public MailBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
 
    public String build(Beneficiary beneficiary) {
    	System.out.println(beneficiary.getName());
        Context context = new Context();
        context.setVariable("beneficiary", beneficiary);
        return templateEngine.process("mailTemplate", context);
    }
    

	public void sendMail(String from, String to, Beneficiary beneficiary) {
    	MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            messageHelper.setSubject("Thank You");
            String content = build(beneficiary);
            messageHelper.setText(content, true);
            
        };
        try {
            mailSender.send(messagePreparator);
        } catch (MailException e) {
            System.out.println(e.getMessage());
        }
    }

}
