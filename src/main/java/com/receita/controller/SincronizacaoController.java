package com.receita.controller;

import com.receita.service.SincronizacaoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

@RestController
@AllArgsConstructor
public class SincronizacaoController {

    private final SincronizacaoService sincronizacaoService;

    @PutMapping("/sync")
    public ResponseEntity process(@RequestBody String tabela) {
        sincronizacaoService.sync(tabela);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/sync")
    public ResponseEntity process() {
        sincronizacaoService.sync();

        return ResponseEntity.ok().build();
    }
}
