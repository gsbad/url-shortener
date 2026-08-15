package dev.gustavosa.urlshortener.service;

import dev.gustavosa.urlshortener.domain.ShortUrl;
import dev.gustavosa.urlshortener.domain.ShortUrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortUrlService {

    private final ShortUrlRepository repository;

    public ShortUrlService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria um código curto para {@code url}.
     *
     * <p>Não há deduplicação: a mesma URL enviada duas vezes recebe dois
     * códigos. Deduplicar exigiria índice sobre a URL e uma consulta a mais
     * por criação, sem contrapartida para o objetivo deste projeto.
     */
    @Transactional
    public ShortUrl shorten(String url) {
        String code = Base62.encode(repository.nextCodeSequence());
        return repository.save(new ShortUrl(code, url));
    }

    @Transactional(readOnly = true)
    public ShortUrl resolve(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ShortUrlNotFoundException(code));
    }
}
