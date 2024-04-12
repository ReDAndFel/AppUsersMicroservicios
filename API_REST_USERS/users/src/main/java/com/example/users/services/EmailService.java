package com.example.users.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.users.models.dtos.EmailDTO;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import io.jsonwebtoken.io.IOException;

@Service
public class EmailService {

    @Value("${PASSWORD_SMTP}")
    private String sendGridApiKey;

    public void sendEmail(EmailDTO emailDTO) throws IOException, java.io.IOException {
        Mail mail = new Mail(
                new Email("pipecar366@gmail.com"),
                emailDTO.getIssuer(),
                new Email(emailDTO.getReceiver()),
                new Content("text/plain", emailDTO.getMessage()));

        // Create a new Request object
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        SendGrid sg = new SendGrid(sendGridApiKey);

        Response response = sg.api(request);

        if (response.getStatusCode() != 202) {
            System.out.println("Error sending email: " + response.getStatusCode() + " - " + response.getBody());
            throw new IOException("Error al enviar el correo");
        }
    }
}