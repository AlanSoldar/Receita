package com.receita.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Conta {

    @NotNull
    @Size(min = 4, max = 4, message = "agencia ${validatedValue} eh invalida. tamanho deve ser entre {min} e {max}.")
    private String agencia;
    @NotNull
    @Size(min = 6, max = 6, message = "conta ${validatedValue} eh invalida. tamanho deve ser entre {min} e {max}.")
    private String conta;
    @NotNull
    private Double saldo;
    @NotNull
    @Size(min = 1, max = 1, message = "status ${validatedValue} eh invalido. tamanho deve ser entre {min} e {max}.")
    private String status;
}
