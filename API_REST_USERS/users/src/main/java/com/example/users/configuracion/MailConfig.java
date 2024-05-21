package com.example.users.configuracion;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
    
    @Value("${HOST_SMTP}")
    private String mailgunHost;

    @Value("${PORT_SMTP}")
    private int mailgunPort;
    
    @Value("${USER_SMTP}")
    private String mailgunUsername;

    @Value("${PASSWORD_SMTP}")
    private String mailgunPass;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailgunHost);
        mailSender.setPort(mailgunPort);
        mailSender.setUsername(mailgunUsername);
        mailSender.setPassword(mailgunPass);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
