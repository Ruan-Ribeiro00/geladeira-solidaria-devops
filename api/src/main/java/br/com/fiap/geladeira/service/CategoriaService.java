package br.com.fiap.geladeira.service;

import br.com.fiap.geladeira.dto.CategoriaRequest;
import br.com.fiap.geladeira.dto.CategoriaResponse;
import br.com.fiap.geladeira.exception.ConflitoException;
import br.com.fiap.geladeira.exception.RecursoNaoEncontradoException;
import br.com.fiap.geladeira.model.Categoria;
import br.com.fiap.geladeira.repository.CategoriaRepository;
import br.com.fiap.geladeira.repository.DoacaoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Categorias quase nunca mudam, mas sao lidas em todo formulario de doacao.
 * Por isso a lista e cada categoria ficam em cache, e qualquer escrita limpa o cache inteiro.
 */
@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final DoacaoRepository doacaoRepository;

    public CategoriaService(CategoriaRepository repository, DoacaoRepository doacaoRepository) {
        this.repository = repository;
        this.doacaoRepository = doacaoRepository;
    }

    @Cacheable(value = "categorias", key = "'todas'")
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(CategoriaService::toResponse).toList();
    }

    @Cacheable(value = "categorias", key = "#id")
    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    @CacheEvict(value = "categorias", allEntries = true)
    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        if (repository.existsByNomeIgnoreCase(req.nome().trim())) {
            throw new ConflitoException("Ja existe uma categoria com o nome '" + req.nome() + "'");
        }
        Categoria c = new Categoria();
        aplicar(c, req);
        return toResponse(repository.save(c));
    }

    @CacheEvict(value = "categorias", allEntries = true)
    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        Categoria c = buscarEntidade(id);
        if (repository.existsByNomeIgnoreCaseAndIdNot(req.nome().trim(), id)) {
            throw new ConflitoException("Ja existe uma categoria com o nome '" + req.nome() + "'");
        }
        aplicar(c, req);
        return toResponse(c);
    }

    @CacheEvict(value = "categorias", allEntries = true)
    @Transactional
    public void excluir(Long id) {
        Categoria c = buscarEntidade(id);
        if (doacaoRepository.existsByCategoriaId(id)) {
            throw new ConflitoException("A categoria tem doacoes registradas e nao pode ser excluida");
        }
        repository.delete(c);
    }

    Categoria buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    private static void aplicar(Categoria c, CategoriaRequest req) {
        c.setNome(req.nome().trim());
        c.setRefrigerado(req.refrigerado());
        c.setTempMaxC(Boolean.TRUE.equals(req.refrigerado()) ? req.tempMaxC() : null);
    }

    static CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.isRefrigerado(), c.getTempMaxC());
    }
}
