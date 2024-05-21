package com.example.users.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailDTO {
    String to;
    String subject;
    String txt;
}
