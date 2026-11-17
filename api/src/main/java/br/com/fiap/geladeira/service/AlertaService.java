package br.com.fiap.geladeira.service;

import br.com.fiap.geladeira.dto.AlertaResponse;
import br.com.fiap.geladeira.exception.RecursoNaoEncontradoException;
import br.com.fiap.geladeira.model.*;
import br.com.fiap.geladeira.repository.AlertaRepository;
import br.com.fiap.geladeira.repository.AlertaSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {

    private final AlertaRepository repository;

    public AlertaService(AlertaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<AlertaResponse> listar(Long geladeiraId, TipoAlerta tipo, NivelAlerta nivel, Boolean resolvido,
                                       Pageable pageable) {
        return repository.findAll(AlertaSpecs.comFiltro(geladeiraId, tipo, nivel, resolvido), pageable)
                .map(AlertaService::toResponse);
    }

    @Transactional(readOnly = true)
    public AlertaResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public AlertaResponse resolver(Long id) {
        Alerta alerta = buscarEntidade(id);
        alerta.setResolvido(true);
        return toResponse(alerta);
    }

    /**
     * Abre um alerta de geladeira, a nao ser que ja exista um aberto do mesmo tipo.
     * @return true se um alerta novo foi criado
     */
    @Transactional
    public boolean abrirParaGeladeira(Geladeira geladeira, TipoAlerta tipo, NivelAlerta nivel, String mensagem) {
        if (repository.existsByGeladeiraIdAndTipoAndResolvidoFalse(geladeira.getId(), tipo)) {
            return false;
        }
        repository.save(Alerta.novo(geladeira, null, tipo, nivel, mensagem));
        return true;
    }

    /** Mesmo que o anterior, mas o alerta e de uma doacao especifica. */
    @Transactional
    public boolean abrirParaDoacao(Doacao doacao, TipoAlerta tipo, NivelAlerta nivel, String mensagem) {
        if (repository.existsByDoacaoIdAndTipoAndResolvidoFalse(doacao.getId(), tipo)) {
            return false;
        }
        repository.save(Alerta.novo(doacao.getGeladeira(), doacao, tipo, nivel, mensagem));
        return true;
    }

    private Alerta buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Alerta", id));
    }

    static AlertaResponse toResponse(Alerta a) {
        return new AlertaResponse(
                a.getId(),
                a.getGeladeira().getId(),
                a.getGeladeira().getNome(),
                a.getDoacao() != null ? a.getDoacao().getId() : null,
                a.getTipo(),
                a.getNivel(),
                a.getMensagem(),
                a.getDtAlerta(),
                a.isResolvido());
    }
}
