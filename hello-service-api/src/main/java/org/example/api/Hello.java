package org.example.api;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Hello implements Serializable {

    private String message;

    private String description;
}
