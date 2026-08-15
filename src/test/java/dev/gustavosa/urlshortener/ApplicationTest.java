package dev.gustavosa.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Garante que o contexto do Spring sobe. É o teste que pega erro de
 * configuração e de injeção de dependência antes de qualquer outro.
 */
@SpringBootTest
class ApplicationTest {

    @Test
    void contextoCarrega() {
    }
}
