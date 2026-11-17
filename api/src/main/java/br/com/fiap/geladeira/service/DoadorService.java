package br.com.fiap.geladeira.service;

import br.com.fiap.geladeira.dto.DoadorRequest;
import br.com.fiap.geladeira.dto.DoadorResponse;
import br.com.fiap.geladeira.exception.ConflitoException;
import br.com.fiap.geladeira.exception.RecursoNaoEncontradoException;
import br.com.fiap.geladeira.model.Doador;
import br.com.fiap.geladeira.model.TipoDoador;
import br.com.fiap.geladeira.repository.DoacaoRepository;
import br.com.fiap.geladeira.repository.DoadorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DoadorService {

    private final DoadorRepository repository;
    private final DoacaoRepository doacaoRepository;

    public DoadorService(DoadorRepository repository, DoacaoRepository doacaoRepository) {
        this.repository = repository;
        this.doacaoRepository = doacaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<DoadorResponse> listar(String nome, TipoDoador tipo, Pageable pageable) {
        Page<Doador> pagina;
        if (nome != null && !nome.isBlank()) {
            pagina = repository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (tipo != null) {
            pagina = repository.findByTipo(tipo, pageable);
        } else {
            pagina = repository.findAll(pageable);
        }
        return pagina.map(DoadorService::toResponse);
    }

    @Transactional(readOnly = true)
    public DoadorResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @Transactional
    public DoadorResponse criar(DoadorRequest req) {
        return toResponse(criarEntidade(req));
    }

    /** Usado tambem pelo cadastro publico de doador (AuthService). */
    @Transactional
    public Doador criarEntidade(DoadorRequest req) {
        if (repository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflitoException("Ja existe um doador com o e-mail " + req.email());
        }
        Doador d = new Doador();
        aplicar(d, req);
        d.setDtCadastro(LocalDate.now());
        return repository.save(d);
    }

    @Transactional
    public DoadorResponse atualizar(Long id, DoadorRequest req) {
        Doador d = buscarEntidade(id);
        if (repository.existsByEmailIgnoreCaseAndIdNot(req.email(), id)) {
            throw new ConflitoException("Ja existe um doador com o e-mail " + req.email());
        }
        aplicar(d, req);
        return toResponse(d);
    }

    @Transactional
    public void excluir(Long id) {
        Doador d = buscarEntidade(id);
        if (doacaoRepository.existsByDoadorId(id)) {
            throw new ConflitoException("O doador tem doacoes registradas e nao pode ser excluido");
        }
        repository.delete(d);
    }

    Doador buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Doador", id));
    }

    private static void aplicar(Doador d, DoadorRequest req) {
        d.setNome(req.nome().trim());
        d.setTipo(req.tipo());
        d.setTelefone(req.telefone());
        d.setEmail(req.email().trim().toLowerCase());
    }

    static DoadorResponse toResponse(Doador d) {
        return new DoadorResponse(d.getId(), d.getNome(), d.getTipo(), d.getTelefone(), d.getEmail(), d.getDtCadastro());
    }
}
