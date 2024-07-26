package com.marcotancredo.minhasfinancas.api.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDTO {
    private String nome;
    private String token;
}
