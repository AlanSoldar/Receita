package com.receita.service;

import com.receita.dto.Conta;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/*
Cenário de Negócio:
Todo dia útil por volta das 6 horas da manhã um colaborador da retaguarda do Sicredi recebe e organiza as informações de contas para enviar ao Banco Central. Todas agencias e cooperativas enviam arquivos Excel à Retaguarda. Hoje o Sicredi já possiu mais de 4 milhões de contas ativas.
Esse usuário da retaguarda exporta manualmente os dados em um arquivo CSV para ser enviada para a Receita Federal, antes as 10:00 da manhã na abertura das agências.

Requisito:
Usar o "serviço da receita" (fake) para processamento automático do arquivo.

Funcionalidade:
0. Criar uma aplicação SprintBoot standalone. Exemplo: java -jar SincronizacaoReceita <input-file>
1. Processa um arquivo CSV de entrada com o formato abaixo.
2. Envia a atualização para a Receita através do serviço (SIMULADO pela classe ReceitaService).
3. Retorna um arquivo com o resultado do envio da atualização da Receita. Mesmo formato adicionando o resultado em uma nova coluna.


Formato CSV:
agencia;conta;saldo;status
0101;12225-6;100,00;A
0101;12226-8;3200,50;A
3202;40011-1;-35,12;I
3202;54001-2;0,00;P
3202;00321-2;34500,00;B
...

*/
@Service
@AllArgsConstructor
@Slf4j
public class SincronizacaoService {

    private final ReceitaService receitaService;
    private final ContaService contaService;

/*
O algoritmo foi imaginado em um cenario onde haveria uma interface web simples onde o usuário poderia arrastar o arquivo e clicar em atualizar.
Então a aplicação é uma api com 2 endpoints.
Um dos endpoints seria a chamada da interface que de preferencia iria traduzir o csv em um json. Mas como essa interface não existe eu estou usando
o postman e enviando no body as informações de agencia, conta, saldo e status no exato formato que foi informado.
O segundo endpoint é para se a pessoa que for testar não quiser usar o postman ou algum programa equivalente. O segundo endpoint é um GET e pode ser
ativado pelo navegador. Este endpoint le um csv dentro do projeto em vez de receber as informações no body de uma chamada http.
Vou descrever como usar ambos os endpoints:

O primeiro endpoint é um PUT na url http://localhost:8080/sync e necessita de um body em raw text no formato edscrito pelo problema (eu copiei e
colei o próprio para testar).

O segundo endpoint é um GET na url http://localhost:8080/sync (a mesma de antes) e funciona simplesmente colando a url no navegador. Como esta url
não recebe body os dados devem ser configurados no arquivo tabela.cvs (src/main/resources/tabela.csv) dentro do projeto
*/
    public void sync(String tabela) {
        /*
        Este método foi pensado com um sistema de mensageria. Eu usaria o RabbitMQ pois é o que ja sei usar porém o rabbitMQ precisa estar instalado
        na maquina que rodar esta aplicação. Por motivos de manter a aplicação "plug and play" eu não usei rabbitMQ mas fiz uma replica do funcionamento
        de forma sincrona.
        Para lidar com erros intermitentes eu fiz um sistema de reprocessing onde se um erro ocorre o registro vai para o fim da fila e é processado novamente
        depois dos outros.
        Usando o RabbitMQ daria para fazer de forma mais eficiente usando paralelismo para deixar a aplicação mais rápida e usando dead letter queues para
        fazer um reprocessamento eficiente e permitindo limitar o número de tentativas por mensagem para não ficar reprocessando para sempre e até mesmo
        salvar as mensagens que passaram do limite de tentativas para análise futura de porque o registro não foi processado.
         */
        List<Conta> fila = contaService.csvToContaList(tabela);
        Conta conta;

        while (!fila.isEmpty()) {
            conta = fila.remove(0);

            try {
                Boolean accepted = receitaService.atualizarConta(conta.getAgencia(), conta.getConta(), conta.getSaldo(), conta.getStatus());
                if (accepted) {
                    log.info("atualizacao concluida para agencia={} e conta={}. Dados atualizados -> salario={} e status={}", conta.getAgencia(), conta.getConta(), conta.getSaldo(), conta.getStatus());
                } else {
                    log.warn("erro");
                }
            } catch (RuntimeException | InterruptedException e) {
                log.warn("Um erro inesperado ocorreu com o registro [agencia={} e conta={}, salario={} e status={}]. O registro sera reprocessado. Erro: {} ", conta.getAgencia(), conta.getConta(), conta.getSaldo(), conta.getStatus(), e.getMessage());
                fila.add(conta);
            }
        }
    }

    public void sync() {
        //Este método le o arquivo tabela.csv e chama o método anterior.
        StringBuilder tabela = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/tabela.csv"));
            while (reader.ready()) {
                tabela.append(reader.readLine()).append('\n');
            }
        } catch (IOException e) {
            log.error("arquivo tabela.csv nao foi encontrado");
        }
        if (!tabela.isEmpty()) {
            sync(tabela.toString());
        }

    }

}
