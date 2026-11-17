package br.com.fiap.geladeira;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de ponta a ponta da API (banco H2 com as mesmas migrations do PostgreSQL).
 * Cada teste roda numa transacao que e desfeita no final, entao um nao interfere no outro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class GeladeiraApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void limparCaches() {
        cacheManager.getCacheNames().forEach(n -> cacheManager.getCache(n).clear());
    }

    private String token(String email, String senha) throws Exception {
        String json = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(json);
        return "Bearer " + node.get("token").asText();
    }

    private String doacaoJson(long geladeira, Long doador, long categoria, String kg, LocalDate validade) {
        return """
               {"geladeiraId":%d,%s"categoriaId":%d,"descricao":"Teste","quantidadeKg":%s,"dtValidade":"%s"}
               """.formatted(geladeira, doador == null ? "" : "\"doadorId\":" + doador + ",", categoria, kg, validade);
    }

    // ------------------------------------------------------------------ seguranca

    @Test
    @DisplayName("Login valido devolve token JWT e senha errada devolve 401")
    void login() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@geladeira.test\",\"senha\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.papel").value("ADMIN"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@geladeira.test\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sem token = 401, papel sem permissao = 403, rotas publicas abertas")
    void autorizacaoPorPapel() throws Exception {
        mvc.perform(get("/api/geladeiras")).andExpect(status().isOk());
        mvc.perform(get("/api/alertas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/alertas").header("Authorization", token("doador@geladeira.test", "Doador@123")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/leituras").header("Authorization", token("voluntario@geladeira.test", "Voluntario@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"geladeiraId\":1,\"temperaturaC\":4,\"portaAberta\":false}"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ CRUD, paginacao, HATEOAS

    @Test
    @DisplayName("Listagem paginada e ordenada com links HATEOAS")
    void paginacaoEHateoas() throws Exception {
        mvc.perform(get("/api/geladeiras").param("size", "3").param("sort", "capacidadeKg,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.geladeiras", hasSize(3)))
                .andExpect(jsonPath("$._embedded.geladeiras[0].capacidadeKg").value(100.0))
                .andExpect(jsonPath("$._embedded.geladeiras[0]._links.self.href").exists())
                .andExpect(jsonPath("$._embedded.geladeiras[0]._links.situacao.href").exists())
                .andExpect(jsonPath("$.page.totalElements").value(10))
                .andExpect(jsonPath("$._links.next.href").exists());
    }

    @Test
    @DisplayName("Bean Validation devolve 400 com a lista de campos invalidos")
    void validacao() throws Exception {
        mvc.perform(post("/api/geladeiras").header("Authorization", token("admin@geladeira.test", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"capacidadeKg\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[*].campo", hasItems("nome", "capacidadeKg", "bairro")));
    }

    @Test
    @DisplayName("Registro inexistente devolve 404 no formato padrao de erro")
    void naoEncontrado() throws Exception {
        mvc.perform(get("/api/geladeiras/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(containsString("999")));
    }

    // ------------------------------------------------------------------ regras de negocio

    @Test
    @DisplayName("RN4: doacao que cabe e registrada (201) e a disponibilidade da doacao gera links de acao")
    void registraDoacao() throws Exception {
        mvc.perform(post("/api/doacoes").header("Authorization", token("voluntario@geladeira.test", "Voluntario@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(5, 5L, 6, "6", LocalDate.now().plusDays(5))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.nivelRisco").value("OK"))
                .andExpect(jsonPath("$._links.retirar.href").exists());
    }

    @Test
    @DisplayName("RN4: doacao que nao cabe devolve 422 e sugere outra geladeira")
    void doacaoNaoCabe() throws Exception {
        mvc.perform(post("/api/doacoes").header("Authorization", token("voluntario@geladeira.test", "Voluntario@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(2, 1L, 5, "10", LocalDate.now().plusDays(30))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.sugestao.geladeiraId").value(7));
    }

    @Test
    @DisplayName("RN4: geladeira em manutencao e alimento refrigerado em geladeira quente sao recusados")
    void doacaoRecusada() throws Exception {
        String vol = token("voluntario@geladeira.test", "Voluntario@123");
        mvc.perform(post("/api/doacoes").header("Authorization", vol).contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(6, 1L, 5, "3", LocalDate.now().plusDays(30))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("MANUTENCAO")));
        mvc.perform(post("/api/doacoes").header("Authorization", vol).contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(3, 3L, 1, "3", LocalDate.now().plusDays(5))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value(containsString("9.8")));
    }

    @Test
    @DisplayName("Doador registra doacao so em nome proprio")
    void doadorEmNomeProprio() throws Exception {
        String doa = token("doador@geladeira.test", "Doador@123");
        mvc.perform(post("/api/doacoes").header("Authorization", doa).contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(1, null, 9, "3", LocalDate.now().plusDays(5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doadorId").value(4));
        mvc.perform(post("/api/doacoes").header("Authorization", doa).contentType(MediaType.APPLICATION_JSON)
                        .content(doacaoJson(1, 2L, 9, "3", LocalDate.now().plusDays(5))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Leitura acima de 8 C abre um alerta de temperatura, sem duplicar")
    void leituraGeraAlerta() throws Exception {
        String sensor = token("sensor@geladeira.test", "Sensor@123");
        String corpo = "{\"geladeiraId\":4,\"temperaturaC\":10.2,\"umidadePct\":60,\"portaAberta\":true}";
        mvc.perform(post("/api/leituras").header("Authorization", sensor).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.foraDoLimite").value(true))
                .andExpect(jsonPath("$.alertaGerado").value(true));
        mvc.perform(post("/api/leituras").header("Authorization", sensor).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(jsonPath("$.alertaGerado").value(false));
    }

    @Test
    @DisplayName("RN5: processamento de riscos descarta os vencidos e marca os criticos")
    void processaRiscos() throws Exception {
        mvc.perform(post("/api/doacoes/processar-riscos").header("Authorization", token("admin@geladeira.test", "Admin@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vencidas").value(2))
                .andExpect(jsonPath("$.critico").value(greaterThan(0)))
                .andExpect(jsonPath("$.alertasGerados").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Filtro dinamico (Specification) combina varios criterios")
    void filtroDinamico() throws Exception {
        mvc.perform(get("/api/doacoes").header("Authorization", token("voluntario@geladeira.test", "Voluntario@123"))
                        .param("geladeiraId", "3").param("refrigerado", "true").param("status", "DISPONIVEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.doacoes", hasSize(2)))
                .andExpect(jsonPath("$._embedded.doacoes[*].categoriaNome", hasItems("Laticinios", "Refeicoes prontas")));
    }

    @Test
    @DisplayName("Ranking de ocupacao (JPQL) comeca pela geladeira mais cheia")
    void rankingOcupacao() throws Exception {
        mvc.perform(get("/api/geladeiras/ocupacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ocupacoes[0].geladeiraId").value(2))
                .andExpect(jsonPath("$._embedded.ocupacoes[0].ocupacaoPct").value(91.7));
    }

    @Test
    @DisplayName("Actuator: health e info publicos, metrics so ADMIN")
    void actuator() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/info")).andExpect(status().isOk());
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics").header("Authorization", token("admin@geladeira.test", "Admin@123")))
                .andExpect(status().isOk());
    }
}
