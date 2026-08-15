package dev.gustavosa.urlshortener.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByCode(String code);

    /**
     * Próximo valor da sequence que alimenta a geração de códigos.
     *
     * <p>{@code nextval} é atômico no PostgreSQL, então dois pedidos
     * concorrentes nunca recebem o mesmo número. É isso que permite gerar
     * código sem tratar colisão nem repetir tentativa.
     */
    @Query(value = "SELECT nextval('short_code_seq')", nativeQuery = true)
    long nextCodeSequence();
}
