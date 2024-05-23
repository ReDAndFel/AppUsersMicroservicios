package com.example.users.services;

import org.aspectj.weaver.ast.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties.Web.Client;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.users.configuracion.MailConfig;
import com.example.users.models.dtos.EmailDTO;

@Service
public class EmailService {

    private static final String MAILGUN_API_URL = "https://api.mailgun.net/v3/";
    private final MailConfig mailConfig;
    private final RestTemplate restTemplate;

    public EmailService(MailConfig mailConfig, RestTemplate restTemplate) {
        this.mailConfig = mailConfig;
        this.restTemplate = restTemplate;
    }

    public void sendMail(EmailDTO emailDTO) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("api", mailConfig.getApiKey());
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("from", mailConfig.getFromEmail());
        body.add("to", emailDTO.getTo());
        body.add("subject", emailDTO.getSubject());
        body.add("text", emailDTO.getTxt());
        body.add("template", "API_USERS");
        body.add("h:X-Mailgun-Variables", "Testing template");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            MAILGUN_API_URL + mailConfig.getDomain() + "/messages",
            HttpMethod.POST,
            request,
            String.class
        ); 

        System.out.println("Response: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }
}