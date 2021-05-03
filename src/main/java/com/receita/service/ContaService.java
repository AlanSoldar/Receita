package com.receita.service;

import com.receita.dto.Conta;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ContaService {
    private final Validator validator;

    public List<Conta> csvToContaList(String tabela) {
        //Este método converte o svc para uma lista de contas
        //idealmente não chegaria aqui um csv e sim um Json que é muito mais simples de converter para um dto
        //As validações dos campos são descritas no DTO de Conta onde diz o tamanho de cada campo
        //Outras validações são feitas aqui mas em geral qualquer erro de entrada não é reprocessado pois nunca funcionará
        //Erros nos campos do arquivo CSV são apenas informados para o usuário em forma de log para que ele possa alterar e tentar outra vez

        List<Conta> contaList = new ArrayList<>();
        String[] linhas = tabela.split("\n");

        if (linhas.length < 2) {
            log.error("nenhum registro foi encontrado");
        } else {

            for (String linha : Arrays.copyOfRange(linhas, 1, linhas.length)) {
                String[] coluna = linha.split(";");
                if (coluna.length == 4) {
                    try {
                        String agencia = coluna[1].replace("-", "");
                        Long.parseLong(coluna[0]);
                        Long.parseLong(agencia);
                        contaList.add(this.newConta(coluna[0], agencia, Double.parseDouble(coluna[2].replace(',', '.')), coluna[3].stripTrailing()));
                    } catch (IllegalArgumentException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }

        return contaList;
    }

    public Conta newConta(String conta, String agencia, Double saldo, String status) {
        //popula o DTO
        Conta novaConta = Conta.builder().agencia(conta).conta(agencia).saldo(saldo).status(status).build();
        this.validate(novaConta);
        return novaConta;
    }

    private void validate(Conta conta) {
        //Aqui é feito o trigger da validação dos campos do DTO Conta
        //Se recebessemos um Json a validação seria feita na conversão do Json para DTO direto no controller por uso da anotação @Validate ou @Valid
        StringBuilder errorMessage = new StringBuilder();
        for (ConstraintViolation<Conta> erro : validator.validate(conta)) {
            errorMessage.append(erro.getMessage()).append(" ");
        }
        if (!errorMessage.isEmpty()) {
            throw new IllegalArgumentException(errorMessage.toString());
        }
    }
}
