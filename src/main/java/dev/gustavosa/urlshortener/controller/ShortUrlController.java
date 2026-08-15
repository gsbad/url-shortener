package dev.gustavosa.urlshortener.controller;

import dev.gustavosa.urlshortener.config.AppProperties;
import dev.gustavosa.urlshortener.domain.ShortUrl;
import dev.gustavosa.urlshortener.service.ShortUrlService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortUrlController {

    private final ShortUrlService service;
    private final AppProperties properties;

    public ShortUrlController(ShortUrlService service, AppProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @PostMapping("/api/urls")
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
        ShortUrl created = service.shorten(request.url());
        ShortUrlResponse body = ShortUrlResponse.from(created, properties.baseUrl());
        return ResponseEntity.created(URI.create(body.shortUrl())).body(body);
    }

    @GetMapping("/api/urls/{code}")
    public ResponseEntity<ShortUrlResponse> get(@PathVariable String code) {
        ShortUrl found = service.resolve(code);
        return ResponseEntity.ok(ShortUrlResponse.from(found, properties.baseUrl()));
    }

    /**
     * Redireciona o código curto para a URL original.
     *
     * <p>302 e não 301: o permanente é cacheado pelo navegador de forma
     * agressiva, e um código gravado errado ficaria impossível de corrigir.
     */
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        ShortUrl found = service.resolve(code);
        return ResponseEntity.status(302).location(URI.create(found.getUrl())).build();
    }
}
