package dev.gustavosa.urlshortener;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShortUrlApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String criar(String url) throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
                put("url", url);
            }});

        String response = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("code").asText();
    }

    @Test
    void criaCodigoParaUrl() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("https://example.com"))
                .andExpect(jsonPath("$.code").value(matchesPattern("^[0-9a-zA-Z]{4,}$")))
                .andExpect(jsonPath("$.shortUrl").value(matchesPattern("^http://localhost:8080/[0-9a-zA-Z]+$")))
                .andExpect(header().exists("Location"));
    }

    @Test
    void resolveUrlPeloCodigo() throws Exception {
        String code = criar("https://example.com/pagina");

        mockMvc.perform(get("/api/urls/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.url").value("https://example.com/pagina"));
    }

    @Test
    void redirecionaParaUrlOriginal() throws Exception {
        String code = criar("https://example.com/destino");

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/destino"));
    }

    @Test
    void codigosSaoUnicosEntreChamadas() throws Exception {
        String primeiro = criar("https://example.com/a");
        String segundo = criar("https://example.com/b");

        org.assertj.core.api.Assertions.assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    void mesmaUrlDuasVezesGeraCodigosDiferentes() throws Exception {
        // Decisão registrada no ADR 0002: não há deduplicação.
        String primeiro = criar("https://example.com/repetida");
        String segundo = criar("https://example.com/repetida");

        org.assertj.core.api.Assertions.assertThat(primeiro).isNotEqualTo(segundo);
    }

    @Test
    void codigoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/urls/{code}", "naoexiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void rejeitaUrlVazia() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.fields.url").exists());
    }

    @Test
    void rejeitaEsquemaNaoHttp() throws Exception {
        // Aceitar esquema arbitrário transformaria a API em vetor de
        // redirecionamento malicioso.
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"));
    }
}
