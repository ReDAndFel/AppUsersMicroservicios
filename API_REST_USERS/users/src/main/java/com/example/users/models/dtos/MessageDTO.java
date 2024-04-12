package com.example.users.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class MessageDTO<T> {

    private HttpStatus status;
    private boolean error;
    private T response;

}