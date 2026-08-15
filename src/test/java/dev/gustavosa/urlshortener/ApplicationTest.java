package dev.gustavosa.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Garante que o contexto sobe. Com JPA e Flyway no projeto, este teste também
 * prova que as migrações aplicam e que o mapeamento das entidades bate com o
 * schema — {@code ddl-auto=validate} falha o contexto se divergir.
 */
@SpringBootTest
class ApplicationTest {

    @Test
    void contextoCarrega() {
    }
}
