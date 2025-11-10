package com.perifaflow.bemestar.service;

import com.perifaflow.bemestar.api.dto.SugestaoMissaoRequest;
import com.perifaflow.bemestar.api.dto.SugestaoMissaoResponse;
import org.springframework.stereotype.Service;

@Service
public class SugestoesService {
    public SugestaoMissaoResponse sugerir(SugestaoMissaoRequest req){
        int soma = nz(req.ultimaEnergia()) + nz(req.ultimoAmbiente()) + nz(req.ultimaCondicao());
        boolean low = soma <= 2; // 0..6
        String complex = low ? "CURTA" : "NORMAL";
        boolean offline = low;
        String mensagem = low
                ? "Dia pesado? Vamos numa missão curtinha/offline pra manter o ritmo."
                : "Boa! Missão normal pra evoluir seu portfólio.";
        return new SugestaoMissaoResponse("SUG-" + req.perfil() + "-" + complex, complex, offline, mensagem);
    }
    private int nz(Integer v){ return v==null?1:v; }
}
