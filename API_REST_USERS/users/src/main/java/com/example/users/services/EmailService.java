package com.example.users.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.users.models.dtos.EmailDTO;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(EmailDTO emailDTO) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDTO.getTo());
        message.setSubject(emailDTO.getSubject());
        message.setText(emailDTO.getTxt());
        mailSender.send(message);
    }
}